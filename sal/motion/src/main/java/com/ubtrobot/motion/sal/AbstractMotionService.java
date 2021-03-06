package com.ubtrobot.motion.sal;

import com.ubtrobot.async.AsyncTask;
import com.ubtrobot.async.InterruptibleAsyncTask;
import com.ubtrobot.async.InterruptibleProgressiveAsyncTask;
import com.ubtrobot.async.ProgressivePromise;
import com.ubtrobot.async.Promise;
import com.ubtrobot.cache.CachedField;
import com.ubtrobot.exception.AccessServiceException;
import com.ubtrobot.master.competition.InterruptibleTaskHelper;
import com.ubtrobot.motion.JointDevice;
import com.ubtrobot.motion.JointException;
import com.ubtrobot.motion.JointGroupRotatingProgress;
import com.ubtrobot.motion.JointRotatingOption;
import com.ubtrobot.motion.LocomotionException;
import com.ubtrobot.motion.LocomotionOption;
import com.ubtrobot.motion.LocomotionProgress;
import com.ubtrobot.motion.LocomotorDevice;
import com.ubtrobot.motion.PerformException;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractMotionService implements MotionService {

    private static final String TASK_RECEIVER_JOINT_PREFIX = "joint-";
    private static final String TASK_RECEIVER_LOCOMOTION = "locomotion";
    private static final String TASK_NAME_JOINT_ROTATE = "joint-rotate";
    private static final String TASK_NAME_JOINT_RELEASE = "joint-release";
    private static final String TASK_NAME_ACTION_EXECUTE = "action-perform";
    private static final String TASK_NAME_LOCOMOTION = "locomotion";

    private final CachedField<Promise<List<JointDevice>, AccessServiceException>> mJoingListPromise;
    private final InterruptibleTaskHelper mInterruptibleTaskHelper;

    public AbstractMotionService() {
        mInterruptibleTaskHelper = new InterruptibleTaskHelper();

        mJoingListPromise = new CachedField<>(new CachedField.FieldGetter<
                Promise<List<JointDevice>, AccessServiceException>>() {
            @Override
            public Promise<List<JointDevice>, AccessServiceException> get() {
                AsyncTask<List<JointDevice>, AccessServiceException> task = createGettingJointListTask();
                if (task == null) {
                    throw new IllegalStateException("createGetJointListTask returns null.");
                }

                task.start();
                return task.promise();
            }
        });
    }

    @Override
    public Promise<List<JointDevice>, AccessServiceException> getJointList() {
        return mJoingListPromise.get();
    }

    protected abstract AsyncTask<List<JointDevice>, AccessServiceException> createGettingJointListTask();

    @Override
    public Promise<List<String>, AccessServiceException>
    isJointsRotating(List<String> jointIdList) {
        AsyncTask<List<String>, AccessServiceException> task = createGettingJointsRotatingTask(jointIdList);
        if (task == null) {
            throw new IllegalStateException("createGetJointsRotatingTask returns null.");
        }

        task.start();
        return task.promise();
    }

    protected abstract AsyncTask<List<String>, AccessServiceException>
    createGettingJointsRotatingTask(List<String> jointIdList);

    @Override
    public Promise<Map<String, Float>, AccessServiceException>
    getJointsAngle(List<String> jointIdList) {
        AsyncTask<Map<String, Float>, AccessServiceException> task
                = createGettingJointsAngleTask(jointIdList);
        if (task == null) {
            throw new IllegalStateException("createGettingJointsAngleTask returns null.");
        }

        task.start();
        return task.promise();
    }

    protected abstract AsyncTask<Map<String, Float>, AccessServiceException>
    createGettingJointsAngleTask(List<String> jointIdList);

    @Override
    public ProgressivePromise<Void, JointException, JointGroupRotatingProgress>
    jointsRotate(final Map<String, List<JointRotatingOption>> optionSequenceMap) {
        LinkedList<String> jointReceivers = new LinkedList<>();
        for (String jointId : optionSequenceMap.keySet()) {
            jointReceivers.add(TASK_RECEIVER_JOINT_PREFIX + jointId);
        }

        final InterruptibleTaskHelper.Session session = new InterruptibleTaskHelper.Session();
        return mInterruptibleTaskHelper.start(
                jointReceivers,
                TASK_NAME_JOINT_ROTATE,
                session,
                new InterruptibleProgressiveAsyncTask<
                        Void, JointException, JointGroupRotatingProgress>() {
                    @Override
                    protected void onStart() {
                        jointStartRotating(session.getId(), optionSequenceMap);
                    }

                    @Override
                    protected void onCancel() {
                        jointStopRotating(session.getId());
                    }
                },
                new InterruptibleTaskHelper.InterruptedExceptionCreator<JointException>() {
                    @Override
                    public JointException createInterruptedException(Set<String> interrupters) {
                        return new JointException.Factory().interrupted("Interrupted by joints("
                                + interrupters + ").");
                    }
                }
        );
    }

    protected abstract void
    jointStartRotating(String sessionId, Map<String, List<JointRotatingOption>> optionSequenceMap);

    protected void jointReportRotatingProgress(String sessionId, JointGroupRotatingProgress progress) {
        mInterruptibleTaskHelper.report(sessionId, progress);
    }

    protected void jointResolveRotating(String sessionId) {
        mInterruptibleTaskHelper.resolve(sessionId, null);
    }

    protected void jointRejectRotating(String sessionId, JointException e) {
        mInterruptibleTaskHelper.reject(sessionId, e);
    }

    protected abstract void
    jointStopRotating(String sessionId);

    @Override
    public Promise<LocomotorDevice, AccessServiceException> getLocomotor() {
        AsyncTask<LocomotorDevice, AccessServiceException> task = createGettingLocomotorTask();
        if (task == null) {
            throw new IllegalStateException("createGettingLocomotorTask return null.");
        }

        task.start();
        return task.promise();
    }

    protected abstract AsyncTask<LocomotorDevice, AccessServiceException> createGettingLocomotorTask();

    @Override
    public Promise<Boolean, AccessServiceException> isLocomoting() {
        AsyncTask<Boolean, AccessServiceException> task = createGettingLocomotingTask();
        if (task == null) {
            throw new IllegalStateException("createGettingLocomotingTask returns null.");
        }

        task.start();
        return task.promise();
    }

    protected abstract AsyncTask<Boolean, AccessServiceException>
    createGettingLocomotingTask();

    @Override
    public ProgressivePromise<Void, LocomotionException, LocomotionProgress>
    locomote(final List<LocomotionOption> optionSequence) {
        return mInterruptibleTaskHelper.start(
                TASK_RECEIVER_LOCOMOTION,
                TASK_NAME_LOCOMOTION,
                new InterruptibleProgressiveAsyncTask<
                        Void, LocomotionException, LocomotionProgress>() {
                    @Override
                    protected void onStart() {
                        startLocomotion(optionSequence);
                    }

                    @Override
                    protected void onCancel() {
                        stopLocomotion();
                    }
                },
                new InterruptibleTaskHelper.InterruptedExceptionCreator<LocomotionException>() {
                    @Override
                    public LocomotionException createInterruptedException(Set<String> interrupters) {
                        return new LocomotionException.Factory().interrupted("Interrupted by "
                                + interrupters + ".");
                    }
                }
        );
    }

    protected abstract void startLocomotion(List<LocomotionOption> optionSequence);

    protected abstract void stopLocomotion();

    protected void reportLocomotionProgress(LocomotionProgress progress) {
        mInterruptibleTaskHelper.report(TASK_RECEIVER_LOCOMOTION, TASK_NAME_LOCOMOTION, progress);
    }

    protected void resolveLocomotion() {
        mInterruptibleTaskHelper.resolve(TASK_RECEIVER_LOCOMOTION, TASK_NAME_LOCOMOTION, null);
    }

    protected void rejectLocomotion(LocomotionException e) {
        mInterruptibleTaskHelper.reject(TASK_RECEIVER_LOCOMOTION, TASK_NAME_LOCOMOTION, e);
    }

    @Override
    public Promise<Void, PerformException> performAction(final List<String> actionIdList) {
        LinkedList<String> receivers = new LinkedList<>();
        try {
            List<JointDevice> jointDevices = mJoingListPromise.get().get();
            for (JointDevice jointDevice : jointDevices) {
                receivers.add(TASK_RECEIVER_JOINT_PREFIX + jointDevice.getId());
            }
            receivers.add(TASK_RECEIVER_LOCOMOTION);
        } catch (AccessServiceException e) {
            throw new IllegalStateException(e);
        }

        final InterruptibleTaskHelper.Session session = new InterruptibleTaskHelper.Session();
        return mInterruptibleTaskHelper.start(
                receivers,
                TASK_NAME_ACTION_EXECUTE,
                session,
                new InterruptibleAsyncTask<Void, PerformException>() {
                    @Override
                    protected void onStart() {
                        startPerformingAction(session.getId(), actionIdList);
                    }

                    @Override
                    protected void onCancel() {
                        stopPerformingAction(session.getId());
                    }
                },
                new InterruptibleTaskHelper.InterruptedExceptionCreator<PerformException>() {
                    @Override
                    public PerformException createInterruptedException(Set<String> interrupters) {
                        return new PerformException.Factory().interrupted("Interrupted by joints("
                                + interrupters + ").");
                    }
                }
        );
    }

    protected abstract void startPerformingAction(String sessionId, List<String> actionIdList);

    protected abstract void stopPerformingAction(String sessionId);

    protected void resolvePerformAction(String sessionId) {
        mInterruptibleTaskHelper.resolve(sessionId, null);
    }

    protected void rejectPerformAction(String sessionId, PerformException e) {
        mInterruptibleTaskHelper.reject(sessionId, e);
    }

    @Override
    public Promise<Void, JointException> jointsRelease(final List<String> jointIdList) {
        LinkedList<String> jointReceivers = new LinkedList<>();
        for (String jointId : jointIdList) {
            jointReceivers.add(TASK_RECEIVER_JOINT_PREFIX + jointId);
        }

        final InterruptibleTaskHelper.Session session = new InterruptibleTaskHelper.Session();
        return mInterruptibleTaskHelper.start(
                jointReceivers,
                TASK_NAME_JOINT_RELEASE,
                session,
                new InterruptibleAsyncTask<Void, JointException>() {
                    @Override
                    protected void onStart() {
                        jointStartReleasing(session.getId(), jointIdList);
                    }

                    @Override
                    protected void onCancel() {
                        jointStopReleasing(session.getId(), jointIdList);
                    }
                },
                new InterruptibleTaskHelper.InterruptedExceptionCreator<JointException>() {
                    @Override
                    public JointException createInterruptedException(Set<String> interrupters) {
                        return new JointException.Factory().interrupted("Interrupted by joints("
                                + interrupters + ").");
                    }
                }
        );
    }

    protected abstract void jointStartReleasing(String sessionId, List<String> jointIdList);

    protected abstract void jointStopReleasing(String sessionId, List<String> jointIdList);

    protected void resolveStartReleasing(String sessionId) {
        mInterruptibleTaskHelper.resolve(sessionId, null);
    }

    protected void rejectStartReleasing(String sessionId, JointException e) {
        mInterruptibleTaskHelper.reject(sessionId, e);
    }

    @Override
    public Promise<List<String>, JointException>
    isJointsReleased(List<String> jointIdList) {
        AsyncTask<List<String>, JointException> task = createGettingJointsReleasedTask(jointIdList);
        if (task == null) {
            throw new IllegalStateException("createGetJointsReleasedTask returns null.");
        }

        task.start();
        return task.promise();
    }

    protected abstract AsyncTask<List<String>, JointException>
    createGettingJointsReleasedTask(List<String> jointIdList);
}