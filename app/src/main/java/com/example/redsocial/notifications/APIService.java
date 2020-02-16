package com.example.redsocial.notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({

            "Content-Type:application/json",
            "Authorization:key=AAAAR2IADhI:APA91bFfRWNpdjQfRORvhrpKOD0dWJdzuoWADxpreNHdHuFq4SvsayfOxhimKlKXp6l7OKBbYB7G1QyGhKM66ARry6sPHMxr09n1X9EZrvNBEN4H5qvUeqV3EUBaCInB9CpNRIvCDYqj"

    })

    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);

}
