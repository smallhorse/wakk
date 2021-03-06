package com.ubtrobot.speech.ipc;

public class SpeechConstant {

    private SpeechConstant() {
    }

    public static final String SERVICE_NAME = "speech";

    public final static String CALL_PATH_SYNTHESIZE = "/speech/synthesize";
    public final static String CALL_PATH_SYNTHESIZING = "/speech/synthesize/doing";

    public final static String CALL_PATH_RECOGNIZE = "/speech/recognize";
    public final static String CALL_PATH_RECOGNIZING = "/speech/recognize/doing";

    public final static String CALL_PATH_UNDERSTAND = "/speech/understand";

    public final static String CALL_PATH_SPEAKER_LIST = "/speech/speaker/list";

    public final static String CALL_PATH_SET_CONFIG = "/speech/configuration/set";
    public final static String CALL_PATH_GET_CONFIG = "/speech/configuration";

    public static final String COMPETING_ITEM_SYNTHESIZER = "synthesizer";
    public static final String COMPETING_ITEM_RECOGNIZER = "recognizer";

    public static final String ACTION_RECOGNIZING = "com.ubtrobot.event.action.RECOGNIZING";
    public static final String ACTION_RECOGNIZE_RESULT =
            "com.ubtrobot.event.action.RECOGNIZE_RESULT";
    public static final String ACTION_RECOGNIZE_ERROR = "com.ubtrobot.event.action.RECOGNIZE_ERROR";
}
