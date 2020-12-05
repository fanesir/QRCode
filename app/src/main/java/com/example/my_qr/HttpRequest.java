package com.example.my_qr;

import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpRequest {
    public static final String PORTOCOL = "http";
    public static final String HOST = "192.168.1.8";
    public static final int PORT = 4000;
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    protected static HttpRequest httpRequest;

    private final OkHttpClient client;

    static class LoginError extends Exception {//伺服器

        public LoginError(String msg) {
            super(msg);
        }
    }

    static class SignUpError extends Exception {
    }

    static class DataError extends Exception {
    }

    static class GetDataError extends DataError {
    }

    static class UpdateDataError extends DataError {
    }

    private HttpRequest() {
        this.client = new OkHttpClient.Builder()
                .cookieJar(new CookieJar() {
                    List<Cookie> list = new ArrayList<>();

                    @Override
                    public void saveFromResponse(@NotNull HttpUrl httpUrl, @NotNull List<Cookie> list) {
                        this.list = list;
                    }

                    @NotNull
                    @Override
                    public List<Cookie> loadForRequest(@NotNull HttpUrl httpUrl) {
                        return this.list;
                    }
                }).build();
    }

    static HttpRequest getInstance() {//HttpRequest new 一個新的
        if (httpRequest == null) {//阻止new 一個新的
            httpRequest = new HttpRequest();
        }
        return httpRequest;
    }

    private HttpUrl.Builder makeURL() {
        return new HttpUrl.Builder()
                .scheme(PORTOCOL)
                .host(HOST)
                .port(PORT);
    }

    private HttpUrl.Builder processPath(HttpUrl.Builder builder, String path) {
        for (String slice : path.split("/")) {
            builder.addPathSegment(slice);
        }
        return builder;
    }

    private Request.Builder Send(String path) throws MalformedURLException {
        return new Request.Builder()
                .url(this.processPath(this.makeURL(), path).build());
    }

    public Response Post(String path, RequestBody body) throws IOException {
        Request request = this.Send(path)
                .post(body)
                .build();
        return client.newCall(request).execute();
    }

    public Response Put(String path, RequestBody body) throws IOException {
        Request request = this.Send(path)
                .method("PUT", body)
                .build();
        return client.newCall(request).execute();
    }

    public Response Get(String path, String[][] args) throws IOException {
        HttpUrl.Builder urlBuilder = this.makeURL();
        this.processPath(urlBuilder, path);
        if (args != null) {
            for (String[] arg : args) {
                urlBuilder.addQueryParameter(arg[0], arg[1]);
            }
        }
        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .get()
                .build();
        return client.newCall(request).execute();
    }

    private RequestBody MakeJson(JSONObject jsonObject) {
        return RequestBody.create(jsonObject.toString(), JSON);
    }

    private RequestBody MakeJson(JSONArray jsonObject) {
        return RequestBody.create(jsonObject.toString(), JSON);
    }

    public void Login(String account, String pasword) throws JSONException, IOException, LoginError {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("account", account);
        jsonObject.put("password", pasword);
        RequestBody body = this.MakeJson(jsonObject);
        Response response = this.Post("/login", body);
        if (response.code() != 200) {
            Log.d("HttpError", response.body().string());
            throw new LoginError("登入失敗");
        }
    }

    public void SignUp(String nickname, String account, String password) throws JSONException, IOException, SignUpError {
        JSONObject object = new JSONObject();//初始進入http
        object.put("nickname", nickname);
        object.put("account", account);
        object.put("password", password);
        RequestBody body = this.MakeJson(object);//收到開始處理資料
        Response response = this.Post("/sign_up", body);//接收 回傳的response
        if (response.code() != 200) {
            Log.d("HttpError", response.body().string());
            throw new SignUpError();
        }
    }

    public ItemInfo GetItem(String item_id) throws IOException, JSONException, GetDataError {
        Response response = this.Get("/api/item", new String[][]{
                {"item_id", item_id}
        });
        if (response.code() != 200) {
            Log.d("HttpError", response.body().string());
            throw new GetDataError();
        }
        return new ItemInfo(new JSONObject(response.body().string()));
    }


    public List<ItemInfo> GetItem(int limit, int offset, ItemState state) throws IOException, JSONException, GetDataError {

        String[][] query;
        if (state == null) {
            query = new String[][]{//Response 請求
                    {"limit", Integer.toString(limit)},
                    {"offset", Integer.toString((offset))}
            };
        } else {
            query = new String[][]{//Response 請求
                    {"limit", Integer.toString(limit)},
                    {"offset", Integer.toString((offset))},
                    {"state", state.toJson().toString()}
            };
        }
        Response response = this.Get("/api/item", query);
        if (response.code() != 200) {
            Log.d("HttpError", response.body().string());
            throw new GetDataError();
        }
        JSONArray result = new JSONArray(response.body().string());
        List<ItemInfo> array = new LinkedList<>();
        for (int i = 0; i < result.length(); ++i) {
            JSONObject data = (JSONObject) result.get(i);

            array.add(new ItemInfo(data));
        }
        return array;
    }


    public List<ItemInfo> searchItem(String EDname) throws IOException, JSONException, GetDataError {

        String[][] query;
        query = new String[][]{//Response 請求
                {"name", (EDname)}

        };
        Response response = this.Get("/api/item", query);
        if (response.code() != 200) {
            Log.d("HttpError", response.body().string());
            throw new GetDataError();
        }
        JSONArray result = new JSONArray(response.body().string());
        List<ItemInfo> array = new LinkedList<>();
        for (int i = 0; i < result.length(); ++i) {
            JSONObject data = (JSONObject) result.get(i);
            array.add(new ItemInfo(data));
        }
        return array;
    }

    //出借
    public void BrrowSignUp(String brrname, String brrnumber) throws JSONException, IOException, SignUpError {
        JSONObject object = new JSONObject();
        object.put("name", brrname);
        object.put("phone", brrnumber);

        RequestBody body = this.MakeJson(object);
        Response response = this.Post("/api/borrower", body);
        if (response.code() != 200) {
            Log.d("HttpError", response.body().string());
            throw new SignUpError();
        }
    }

    public List<BrItemInfo> BrGetItem(int limit, int offset) throws IOException, JSONException, GetDataError {

        String[][] query;

        query = new String[][]{//Response 請求
                {"limit", Integer.toString(limit)},
                {"offset", Integer.toString((offset))}

        };

        Response response = this.Get("/api/borrower", query);
        if (response.code() != 200) {
            Log.i("HttpError", response.body().string());
            throw new GetDataError();
        }
        JSONArray resultt = new JSONArray(response.body().string());
        List<BrItemInfo> array = new LinkedList<>();
        for (int i = 0; i < resultt.length(); ++i) {

            JSONObject data = (JSONObject) resultt.get(i);

            array.add(new BrItemInfo(data));
        }
        return array;
    }

    public void UpdataBrItem(String brname, String brphone, int brItem) throws IOException, JSONException, UpdateDataError {
        JSONObject body = new JSONObject();
        body.put("id", brItem);
        body.put("name", brname);
        body.put("phone", brphone);

        Response response = this.Put("/api/borrower", this.MakeJson(body));
        if (response.code() != 200) {
            Log.d("HttpError", response.body().string());
            throw new UpdateDataError();
        }

    }


    static class ItemState {
        public String location;
        public boolean correct = false;
        public boolean discard = false;
        public boolean fixing = false;
        public boolean unlabel = false;

        public JSONObject toJson() throws JSONException {
            JSONObject object = new JSONObject();
            object.put("location", location);
            object.put("correct", correct);
            object.put("discard", discard);
            object.put("fixing", fixing);
            object.put("unlabel", unlabel);
            return object;
        }
    }

    public void UpdateItem(String item_id, String location, String note, ItemState state) throws IOException, JSONException, UpdateDataError {

        JSONObject body = new JSONObject();
        body.put("item_id", item_id);
        if (location != null) {
            body.put("locaion", location);
        }
        if (note != null) {
            body.put("note", note);
        }
        if (state != null) {//如果裡面不是空白跑這裡
            JSONObject stateJson = new JSONObject();
            stateJson.put("correct", state.correct);
            stateJson.put("discard", state.discard);
            stateJson.put("fixing", state.fixing);
            stateJson.put("unlabel", state.unlabel);
            body.put("state", stateJson);

        }
        Response response = this.Put("/api/item", this.MakeJson(body));
        if (response.code() != 200) {
            Log.d("HttpError", response.body().string());
            throw new UpdateDataError();
        }
    }


    static class ItemInfo extends JsonData implements Serializable {
        protected Integer age_limit;
        protected Integer cost;
        protected String date;
        protected String item_id;
        protected String location;
        protected String name;
        protected String note;

        protected boolean correct;
        protected boolean discard;
        protected boolean fixing;
        protected boolean unlabel;

        protected String brname;
        protected String brnumber;

        ItemInfo(JSONObject object) throws JSONException {
            super(object);
            this.age_limit = this.defaultGet("age_limit", 0);//為了不讓它重新創建資料型別(Integer)
            this.cost = this.defaultGet("cost", 0);
            this.date = this.defaultGet("date", "");
            this.item_id = this.mustGet("item_id");
            this.location = this.mustGet("location");
            this.name = this.mustGet("name");
            this.note = this.mustGet("note");
            this.brname = this.mustGet("name");
            // this.brnumber = this.mustGet("phone");

            JSONObject state = this.mustGet("state");
            this.correct = state.getBoolean("correct");
            this.discard = state.getBoolean("discard");
            this.fixing = state.getBoolean("fixing");
            this.unlabel = state.getBoolean("unlabel");
        }
    }

    static class BrItemInfo extends JsonData implements Serializable {

        protected int id;
        protected String brname;
        protected String brnumber;

        BrItemInfo(JSONObject object) throws JSONException {
            super(object);

            this.id = this.mustGet("id");
            this.brname = this.mustGet("name");
            this.brnumber = this.mustGet("phone");


        }
    }
}


class Person {
    protected final String name;

    Person(String name) {
        this.name = name;
    }

    String introduce() {
        return this.name;
    }
}

class Main {


    static public void main(String[] arg) {
        Person person1 = new Person("A");
        Person person2 = new Person("B");
        Person person3 = new Person("C");
        person1.introduce();
        person2.introduce();
        person3.introduce();
        String a = person3.name;
    }
}