package com.ubtrobot.motion.ipc;

public class MotionConstants {

    private MotionConstants() {
    }

    public static final String SERVICE_NAME = "motion";

    public static final String COMPETING_ITEM_PREFIX_JOINT = "joint-";
    public static final String COMPETING_ITEM_ACTION_PERFORMER = "motion-action-performer";
    public static final String COMPETING_ITEM_LOCOMOTOR = "locomotor";

    public static final String CALL_PATH_GET_JOINT_LIST = "/joint/device/list";
    public static final String CALL_PATH_QUERY_JOINT_IS_ROTATING = "/joint/query-is-rotating";
    public static final String CALL_PATH_GET_JOINT_ANGLE = "/joint/angle";
    public static final String CALL_PATH_JOINT_ROTATE = "/joint/rotate";
    public static final String CALL_PATH_PERFORM_MOTION_ACTION = "/motion/action/perform";
    public static final String CALL_PATH_JOINT_RELEASE = "/joint/release";
    public static final String CALL_PATH_QUERY_JOINT_IS_RELEASED= "/joint/query-is-released";

    public static final String CALL_PATH_GET_LOCOMOTOR = "/locomotor/device";
    public static final String CALL_PATH_LOCOMOTE = "/locomotor/locomote";
    public static final String CALL_PATH_QUERY_IS_LOCOMOTING = "/locomotor/query-is-locomoting";
}