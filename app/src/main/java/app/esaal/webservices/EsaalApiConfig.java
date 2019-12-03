package app.esaal.webservices;

import app.esaal.classes.Constants;
import retrofit.RestAdapter;

public class EsaalApiConfig {

    public static EsaalApiInterface apiInterface;

    public static EsaalApiInterface getCallingAPIInterface(){
        if(apiInterface == null){
            try {
                RestAdapter restAdapter = new RestAdapter.Builder().setLogLevel(RestAdapter.LogLevel.FULL)
                        .setEndpoint(Constants.BASE_URL).build();
                apiInterface = restAdapter.create(EsaalApiInterface.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return apiInterface;
    }

}
