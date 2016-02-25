package com.tlongdev.bktf;

import com.tlongdev.bktf.api.FixerIoInterface;
import com.tlongdev.bktf.model.CurrencyRates;
import com.tlongdev.bktf.network.TestInterceptor;
import com.tlongdev.bktf.util.TestUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class FixerIoInterfaceTest {

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFixerIoParsing() throws IOException {

        InputStream is = getClass().getClassLoader().getResourceAsStream("fixer_io_mock.json");
        Assert.assertNotNull(is);

        String dummyResponse = TestUtils.convertStreamToString(is);
        Assert.assertNotNull(dummyResponse);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new TestInterceptor(dummyResponse, "json"))
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(FixerIoInterface.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        FixerIoInterface service = retrofit.create(FixerIoInterface.class);

        Response<CurrencyRates> response = service.getCurrencyRates("USD").execute();

        CurrencyRates rates = response.body();
        Assert.assertNotNull(rates);

        Assert.assertEquals("USD", rates.getBase());
        Assert.assertEquals("2016-02-25", rates.getDate());

        Assert.assertEquals(rates.getRates().get("AUD"), 1.3868, 0);
        Assert.assertEquals(rates.getRates().get("BGN"), 1.7736, 0);
        Assert.assertEquals(rates.getRates().get("BRL"), 3.9473, 0);
        Assert.assertEquals(rates.getRates().get("CAD"), 1.3626, 0);
        Assert.assertEquals(rates.getRates().get("CHF"), 0.99166, 0);
        Assert.assertEquals(rates.getRates().get("CNY"), 6.5318, 0);
        Assert.assertEquals(rates.getRates().get("CZK"), 24.533, 0);
        Assert.assertEquals(rates.getRates().get("DKK"), 6.7668, 0);
        Assert.assertEquals(rates.getRates().get("GBP"), 0.7157, 0);
        Assert.assertEquals(rates.getRates().get("HKD"), 7.7697, 0);
        Assert.assertEquals(rates.getRates().get("HRK"), 6.9121, 0);
        Assert.assertEquals(rates.getRates().get("HUF"), 281.91, 0);
        Assert.assertEquals(rates.getRates().get("IDR"), 13411.0, 0);
        Assert.assertEquals(rates.getRates().get("ILS"), 3.9009, 0);
        Assert.assertEquals(rates.getRates().get("INR"), 68.875, 0);
        Assert.assertEquals(rates.getRates().get("JPY"), 112.46, 0);
        Assert.assertEquals(rates.getRates().get("KRW"), 1239.4, 0);
        Assert.assertEquals(rates.getRates().get("MXN"), 18.138, 0);
        Assert.assertEquals(rates.getRates().get("MYR"), 4.2169, 0);
        Assert.assertEquals(rates.getRates().get("NOK"), 8.6527, 0);
        Assert.assertEquals(rates.getRates().get("NZD"), 1.4951, 0);
        Assert.assertEquals(rates.getRates().get("PHP"), 47.639, 0);
        Assert.assertEquals(rates.getRates().get("PLN"), 3.9522, 0);
        Assert.assertEquals(rates.getRates().get("RON"), 4.0512, 0);
        Assert.assertEquals(rates.getRates().get("RUB"), 76.138, 0);
        Assert.assertEquals(rates.getRates().get("SEK"), 8.5071, 0);
        Assert.assertEquals(rates.getRates().get("SGD"), 1.4023, 0);
        Assert.assertEquals(rates.getRates().get("THB"), 35.66, 0);
        Assert.assertEquals(rates.getRates().get("TRY"), 2.9245, 0);
        Assert.assertEquals(rates.getRates().get("ZAR"), 15.567, 0);
        Assert.assertEquals(rates.getRates().get("EUR"), 0.90686, 0);
    }
}