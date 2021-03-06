package com.ubtrobot.dance.player;

import android.util.Log;

import com.ubtrobot.async.DoneCallback;
import com.ubtrobot.async.FailCallback;
import com.ubtrobot.async.Promise;
import com.ubtrobot.motion.MotionManager;
import com.ubtrobot.motion.PerformException;
import com.ubtrobot.play.AbstractSegmentPlayer;
import com.ubtrobot.play.PlayException;
import com.ubtrobot.play.Segment;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;

import org.json.JSONException;
import org.json.JSONObject;

public class ArmMotionSegmentPlayer extends AbstractSegmentPlayer<
        ArmMotionSegmentPlayer.ArmMotionOption> {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("ArmMotionSegmentPlayer");

    private MotionManager mMotionManager;
    private Promise<Void, PerformException> mExecutePromise;

    public ArmMotionSegmentPlayer(MotionManager motionManager, Segment segment) {
        super(segment);
        mMotionManager = motionManager;
    }

    public static ArmMotionOption parser(JSONObject optionJson) {
        try {
            return new ArmMotionOption(optionJson.getString(ArmMotionOptionKey.ID));
        } catch (JSONException e) {
            throw new IllegalStateException("Please check json: motion track.");
        }
    }

    @Override
    protected void onLoopStart(ArmMotionOption option) {
        mMotionManager.performAction(option.getId()).done(new DoneCallback<Void>() {
            @Override
            public void onDone(Void aVoid) {
                LOGGER.w("Arm motion onDone...");
                mExecutePromise = null;
            }
        }).fail(new FailCallback<PerformException>() {
            @Override
            public void onFail(PerformException e) {
                LOGGER.w("Arm motion onFail:" + e.toString());
                notifyLoopAborted(new PlayException.Factory().from(e.getCode(), e.getMessage()));
                mExecutePromise = null;
            }
        });
    }

    @Override
    protected void onLoopStop() {
        LOGGER.w("Arm motion onLoopStop...");
        cancel();
    }

    private void cancel() {
        if (mExecutePromise != null) {
            LOGGER.w("Arm motion mExecutePromise is not null...");
            mExecutePromise.cancel();
            mExecutePromise = null;
        }

        LOGGER.w("Arm motion executeScript reset...");
        mMotionManager.performAction("reset").done(new DoneCallback<Void>() {
            @Override
            public void onDone(Void aVoid) {
                LOGGER.w("Arm motion executeScript reset onDone...");
            }
        }).fail(new FailCallback<PerformException>() {
            @Override
            public void onFail(PerformException e) {
                LOGGER.e("Arm motion executeScript reset onFail:" + e);
                notifyLoopAborted(new PlayException.Factory().
                        forbidden(e.getMessage(), e.getDetail()));
            }
        });
    }

    @Override
    protected void onEnd() {
        Log.i("cj", "onEnd: 动作");
        cancel();
    }

    private static final class ArmMotionOptionKey {

        private static final String ID = "id";

        private ArmMotionOptionKey() {
        }
    }

    public static class ArmMotionOption {

        private String id;

        public ArmMotionOption(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        @Override
        public String toString() {
            return "ArmMotionOption{" +
                    "id='" + id + '\'' +
                    '}';
        }
    }
}
