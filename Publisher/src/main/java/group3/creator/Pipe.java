package group3.creator;

import org.json.JSONObject;

public class Pipe {
    private static final String PIPEMANAGER_TO_BOTTLENECK = "pipeManagerToBottleneck";
    private static final String PIPEMANAGER_TO_BLINDSPOT = "pipeManagerToBlindspot";
    private static final String PIPEMANAGER_TO_EMITTER = "pipeManagerToEmitter";


    public JSONObject transfer(JSONObject data, String fromTo, String topic) {
        switch (fromTo) {
            case PIPEMANAGER_TO_BOTTLENECK:
                BottleneckFilter filterBottle = new BottleneckFilter(data);
                return filterBottle.getData();

            case PIPEMANAGER_TO_BLINDSPOT:
                BlindspotFilter filterBlind = new BlindspotFilter(data);
                return filterBlind.getData();

            case PIPEMANAGER_TO_EMITTER:
                PipeManager.emitter.emitter(data, topic);
                break;
        }
        return null;
    }
}