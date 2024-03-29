package com.example.my_qr;

import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
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
    public static final String PROTOCOL = "http";
    public static final String HOST = "140.128.75.190";// "1"http://140.128.75.190:4000/
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
        client = new OkHttpClient.Builder()//登入帳號使用
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
                .scheme(PROTOCOL)
                .host(HOST)
                .port(PORT);

    }

    private HttpUrl.Builder processPath(HttpUrl.Builder builder, String path) {
        for (String slice : path.split("/")) {
            builder.addPathSegment(slice);
        }
        return builder;
    }

    private Request.Builder Send(String path) {//"http"192.168.1.18/4000 /api/item etc...
        return new Request.Builder()
                .url(this.processPath(this.makeURL(), path).build());

    }

    public Response Post(String path, RequestBody body) throws IOException {
        Request request = this.Send(path)//把//api/item 等等放入
                .post(body)//輸入的資料
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

    public void Login(String account, String password) throws JSONException, IOException, LoginError {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("account", account);
        jsonObject.put("password", password);
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

    public ItemInfo GetItem(int item_id) throws IOException, JSONException, GetDataError {
        Response response = this.Get("/api/item", new String[][]{
                {"id", item_id+""}
        });
        if (response.code() != 200) {
            Log.d("HttpError", response.body().string());
            throw new GetDataError();
        }
        return new ItemInfo(new JSONObject(response.body().string()));
    }

    public List<ItemInfo> GetItem(int limit, int offset, ItemState state ) throws IOException, JSONException, GetDataError {
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
                    {"state", state.toJson().toString()},

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


    public List<ItemInfo> SearchItem(String EDname) throws IOException, JSONException, GetDataError {
        String[][] query = new String[][]{
                {"name", EDname},
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
    public BorrowerInfo CreateBorrower(String name, String phone_number) throws JSONException, IOException, SignUpError {
        JSONObject object = new JSONObject();

        object.put("name", name);
        object.put("phone", phone_number);


        RequestBody body = this.MakeJson(object);
        Response response = this.Post("/api/borrower", body);
        if (response.code() != 200) {
            Log.d("HttpError", response.body().string());
            throw new SignUpError();
        }
        return new BorrowerInfo(new JSONObject(response.body().string()));

    }

    //增加此借出人清單
    public void CreateThisAccountBorrowerItem(int id, String borrow_date, String reply_date, String note, int BrrowItemID) throws JSONException, IOException, SignUpError {

        JSONObject object = new JSONObject();
        object.put("borrower_id", id);
        object.put("borrow_date", borrow_date);//
        object.put("reply_date", reply_date);//
        object.put("note", note);
        object.put("item_id", BrrowItemID);

        RequestBody body = this.MakeJson(object);
        Response response = this.Post("/api/borrow_record", body);
        if (response.code() != 200) {
            Log.d("HttpError", response.body().string());
            throw new SignUpError();
        }
    }

    public void ThisAccountBorrowerItemUpreturned(int id,Boolean returned ) throws JSONException, IOException, SignUpError {

        JSONObject object = new JSONObject();
        object.put("id", id);
        object.put("returned", returned);//


        RequestBody body = this.MakeJson(object);
        Response response = this.Put("/api/borrow_record", body);
        if (response.code() != 200) {
            Log.d("HttpError", response.body().string()+response.code()+"");
            throw new SignUpError();
        }
    }



    public BorrowerInfo GetBorrower(int id) throws IOException, JSONException {
        Response response = this.Get("/api/borrower", new String[][]{
            {"id", "" + id},
        });
        return new BorrowerInfo(new JSONObject(response.body().string()));
    }

    public List<BorrowerInfo> GetBorrowerList(int limit, int offset) throws IOException, JSONException, GetDataError {
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
        List<BorrowerInfo> array = new LinkedList<>();
        for (int i = 0; i < resultt.length(); ++i) {
            JSONObject data = (JSONObject) resultt.get(i);//json拿到的
            array.add(new BorrowerInfo(data));//放進去陣列
        }
        return array;
    }



    public BorrowRecord GetBorrowerRecord(int id) throws IOException, JSONException, GetDataError {
        String[][] query;

        query = new String[][]{//Response 請求
                {"id", Integer.toString(id)},

        };
        Response response = this.Get("/api/borrow_record", query);

        JSONObject result = new JSONObject(response.body().string());
        return new BorrowRecord(result);
    }

    public List<BorrowRecord> GetBorrowerRecord(int limit, int offset) throws IOException, JSONException, GetDataError {
        String[][] query;

        query = new String[][]{//Response 請求
                {"limit", Integer.toString(limit)},
                {"offset", Integer.toString((offset))}
        };

        Response response = this.Get("/api/borrow_record", query);
        if (response.code() != 200) {
            Log.i("HttpError", response.body().string());
            throw new GetDataError();
        }
        JSONArray result = new JSONArray(response.body().string());

        List<BorrowRecord> array = new LinkedList<>();

        for (int i = 0; i < result.length(); ++i) {
            JSONObject data = result.getJSONObject(i);   //json拿到的
            array.add(new BorrowRecord(data));//放進去陣列
        }
        return array;
    }

    public List<BorrowRecord> ItemidListview(int limit ,int offset,int item_id) throws IOException, JSONException, GetDataError {
        String[][] query = new String[][]{
                {"limit", Integer.toString(limit)},
                {"offset", Integer.toString((offset))},
                {"item_id", Integer.toString(item_id)}

        };

        Response response = this.Get("/api/borrow_record", query);
        if (response.code() != 200) {
            Log.d("HttpError", response.body().string());
            throw new GetDataError();
        }
        JSONArray result = new JSONArray(response.body().string());
        List<BorrowRecord> array = new LinkedList<>();
        for (int i = 0; i < result.length(); ++i) {
            JSONObject data = (JSONObject) result.get(i);
            array.add(new BorrowRecord(data));
        }
        return array;
    }


    public void UpdateBorrower(String name, String phone_number, int BorrowedItemID) throws IOException, JSONException, UpdateDataError {

        JSONObject body = new JSONObject();

        body.put("id", BorrowedItemID);
        body.put("name", name);
        body.put("phone", phone_number);

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
            body.put("location", location);
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
        protected Integer id;

        protected boolean correct;
        protected boolean discard;
        protected boolean fixing;
        protected boolean unlabel;

        ItemInfo(JSONObject object) throws JSONException {
            super(object);
            this.age_limit = this.defaultGet("age_limit", 0);
            this.cost = this.defaultGet("cost", 0);
            this.date = this.defaultGet("date", "");
            this.item_id = this.mustGet("item_id");
            this.id = this.mustGet("id");
            this.location = this.mustGet("location");
            this.name = this.mustGet("name");
            this.note = this.mustGet("note");

            JSONObject state = this.mustGet("state");
            this.correct = state.getBoolean("correct");
            this.discard = state.getBoolean("discard");
            this.fixing = state.getBoolean("fixing");
            this.unlabel = state.getBoolean("unlabel");
        }
    }

    static class BorrowerInfo extends JsonData implements Serializable {
        protected int id;
        protected String name;
        protected String phone_number;

        BorrowerInfo(JSONObject object) throws JSONException {
            super(object);

            this.id = this.mustGet("id");
            this.name = this.mustGet("name");
            this.phone_number = this.mustGet("phone");
        }
    }

     static class BorrowRecord extends JsonData implements Serializable {
        protected int id;
        protected String borrow_date ;
        protected String reply_date;
        protected String note;
        protected int item_id;
        protected int borrower_id;

        BorrowRecord(JSONObject object) throws JSONException {
            super(object);

            this.id = this.mustGet("id");//Brrow Account item
            this.item_id = this.mustGet("item_id");//Brrow item
            this.borrower_id = this.mustGet("borrower_id");
            this.borrow_date = this.mustGet("borrow_date");
            this.note = this.mustGet("note");
            this.reply_date = this.mustGet("reply_date")+"";


        }
    }


}