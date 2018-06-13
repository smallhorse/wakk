package com.ubtrobot.power;

import android.os.Handler;
import android.os.Looper;

import com.google.protobuf.BoolValue;
import com.ubtrobot.async.Consumer;
import com.ubtrobot.async.ListenerList;
import com.ubtrobot.async.Promise;
import com.ubtrobot.exception.AccessServiceException;
import com.ubtrobot.master.adapter.CallAdapter;
import com.ubtrobot.master.adapter.ProtoCallAdapter;
import com.ubtrobot.master.adapter.ProtoEventReceiver;
import com.ubtrobot.master.context.MasterContext;
import com.ubtrobot.power.ipc.PowerConstants;
import com.ubtrobot.power.ipc.PowerConverters;
import com.ubtrobot.power.ipc.PowerProto;
import com.ubtrobot.transport.message.CallException;

public class PowerManager {

    private final MasterContext mMasterContext;
    private ProtoCallAdapter mService;

    private final ListenerList<BatteryListener> mListenerList;
    private final BatteryEventReceiver mBatterEventReceiver = new BatteryEventReceiver();

    public PowerManager(MasterContext masterContext) {
        mMasterContext = masterContext;
        Handler handler = new Handler(Looper.getMainLooper());
        mService = new ProtoCallAdapter(masterContext.createSystemServiceProxy(
                PowerConstants.SERVICE_NAME), handler);
        mListenerList = new ListenerList<>(handler);
    }

    public Promise<Boolean, AccessServiceException> sleep() {
        return mService.call(
                PowerConstants.CALL_PATH_SLEEP,
                new ProtoCallAdapter.DFProtoConverter<Boolean, BoolValue, AccessServiceException>() {
                    @Override
                    public Class<BoolValue> doneProtoClass() {
                        return BoolValue.class;
                    }

                    @Override
                    public Boolean convertDone(BoolValue notSleeping) throws Exception {
                        return notSleeping.getValue();
                    }

                    @Override
                    public AccessServiceException convertFail(CallException e) {
                        return new AccessServiceException.Factory().from(e);
                    }
                }
        );
    }

    public Promise<Boolean, AccessServiceException> isSleeping() {
        return mService.call(
                PowerConstants.CALL_PATH_QUERY_IS_SLEEPING,
                new ProtoCallAdapter.DFProtoConverter<Boolean, BoolValue, AccessServiceException>() {
                    @Override
                    public Class<BoolValue> doneProtoClass() {
                        return BoolValue.class;
                    }

                    @Override
                    public Boolean convertDone(BoolValue sleeping) throws Exception {
                        return sleeping.getValue();
                    }

                    @Override
                    public AccessServiceException convertFail(CallException e) {
                        return new AccessServiceException.Factory().from(e);
                    }
                }
        );
    }

    public Promise<Boolean, AccessServiceException> wakeUp() {
        return mService.call(
                PowerConstants.CALL_PATH_WAKE_UP,
                new ProtoCallAdapter.DFProtoConverter<Boolean, BoolValue, AccessServiceException>() {
                    @Override
                    public Class<BoolValue> doneProtoClass() {
                        return BoolValue.class;
                    }

                    @Override
                    public Boolean convertDone(BoolValue notWakeUp) throws Exception {
                        return notWakeUp.getValue();
                    }

                    @Override
                    public AccessServiceException convertFail(CallException e) {
                        return new AccessServiceException.Factory().from(e);
                    }
                }
        );
    }

    public Promise<Void, AccessServiceException> shutdown() {
        return shutdown(ShutdownOption.DEFAULT);
    }

    public Promise<Void, AccessServiceException> shutdown(ShutdownOption option) {
        return mService.call(
                PowerConstants.CALL_PATH_SHUTDOWN,
                PowerConverters.toShutdownOptionProto(option),
                new CallAdapter.FConverter<AccessServiceException>() {
                    @Override
                    public AccessServiceException convertFail(CallException e) {
                        return new AccessServiceException.Factory().from(e);
                    }
                }
        );
    }

    public Promise<BatteryProperties, AccessServiceException> getBatteryProperties() {
        return mService.call(
                PowerConstants.CALL_PATH_GET_BATTERY_PROPERTIES,
                new ProtoCallAdapter.DFProtoConverter<
                        BatteryProperties, PowerProto.BatteryProperties, AccessServiceException>() {
                    @Override
                    public Class<PowerProto.BatteryProperties> doneProtoClass() {
                        return PowerProto.BatteryProperties.class;
                    }

                    @Override
                    public BatteryProperties
                    convertDone(PowerProto.BatteryProperties properties) throws Exception {
                        return PowerConverters.toBatteryPropertiesPojo(properties);
                    }

                    @Override
                    public AccessServiceException convertFail(CallException e) {
                        return new AccessServiceException.Factory().from(e);
                    }
                }
        );
    }

    public void registerBatteryListener(BatteryListener listener) {
        synchronized (mListenerList) {
            boolean subscribed = !mListenerList.isEmpty();
            mListenerList.register(listener);

            if (!subscribed) {
                mMasterContext.subscribe(mBatterEventReceiver, PowerConstants.ACTION_BATTERY_CHANGE);
            }
        }
    }

    public void unregisterBatteryListener(BatteryListener listener) {
        synchronized (mListenerList) {
            mListenerList.unregister(listener);

            if (mListenerList.isEmpty()) {
                mMasterContext.unsubscribe(mBatterEventReceiver);
            }
        }
    }

    private class BatteryEventReceiver extends ProtoEventReceiver<PowerProto.BatteryProperties> {
        @Override
        protected Class<PowerProto.BatteryProperties> protoClass() {
            return PowerProto.BatteryProperties.class;
        }

        @Override
        public void onReceive(
                MasterContext masterContext,
                String action,
                final PowerProto.BatteryProperties properties) {
            synchronized (mListenerList) {
                final BatteryProperties batteryProperties =
                        PowerConverters.toBatteryPropertiesPojo(properties);
                mListenerList.forEach(new Consumer<BatteryListener>() {
                    @Override
                    public void accept(BatteryListener listener) {
                        listener.onBatteryChanged(batteryProperties);
                    }
                });
            }
        }
    }
}
