public class CastApiManager {

    private static CastApiManager instance;
    private String appId;
    private GoogleApiClient apiClient;
    private CastChannel castChannel;

    private CastApiManager() {
    }

    private CastApiManager(String appId, CastChannel castChannel) {
        this.appId = appId;
        this.castChannel = castChannel;
    }

    public static void init(String appId, CastChannel castChannel) {
        if (instance == null) {
            instance = new CastApiManager(appId, castChannel);
        }
    }

    public static CastApiManager getInstance() {
        return instance;
    }

    public void prepareConnection(Context context, CastDevice selectedDevice) {
        if ((apiClient == null || !apiClient.isConnected()) && context != null && selectedDevice != null) {
            Cast.CastOptions.Builder apiOptionsBuilder = new Cast.CastOptions.Builder(selectedDevice, prepareCastClientListener());

            apiClient = new GoogleApiClient.Builder(context)
                    .addApi(Cast.API, apiOptionsBuilder.build())
                    .addConnectionCallbacks(prepareConnectionCallbacks())
                    .addOnConnectionFailedListener(prepareConnectionFailedListener())
                    .build();

            apiClient.connect();
            ColorTvCastSDK.setApiClient(apiClient);
        }
    }

    

    public void setUpChannel() {
        try {
            Cast.CastApi.setMessageReceivedCallbacks(apiClient, castChannel.getNamespace(), castChannel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeChannel() {
        try {
            Cast.CastApi.removeMessageReceivedCallbacks(apiClient, castChannel.getNamespace());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public GoogleApiClient getApiClient() {
        return apiClient;
    }

    public void sendMessage(String message) {
        if (isConnected()) {
            Cast.CastApi.sendMessage(apiClient, castChannel.getNamespace(), message);
        }
    }


    public boolean isConnected() {
        return apiClient != null && apiClient.isConnected();
    }
}