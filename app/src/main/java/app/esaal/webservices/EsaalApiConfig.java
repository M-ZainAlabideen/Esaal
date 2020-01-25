package app.esaal.webservices;
import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.TimeUnit;

import app.esaal.classes.Constants;
import retrofit.RestAdapter;
import retrofit.client.OkClient;

public class EsaalApiConfig {

    public static EsaalApiInterface apiInterface;

    public static EsaalApiInterface getCallingAPIInterface(){
        if(apiInterface == null){
            try {

                final OkHttpClient okHttpClient = new OkHttpClient();
                okHttpClient.setReadTimeout(10, TimeUnit.MINUTES);
                okHttpClient.setWriteTimeout(10,TimeUnit.MINUTES);
                okHttpClient.setConnectTimeout(10, TimeUnit.MINUTES);

                RestAdapter restAdapter = new RestAdapter.Builder()
                        .setLogLevel(RestAdapter.LogLevel.FULL)
                        .setEndpoint(Constants.BASE_URL)
                        .setClient(new OkClient(okHttpClient))
                        .build();

                apiInterface = restAdapter.create(EsaalApiInterface.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return apiInterface;
    }

}
