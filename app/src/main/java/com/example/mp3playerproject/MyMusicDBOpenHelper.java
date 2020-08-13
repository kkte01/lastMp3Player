package com.example.mp3playerproject;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.Nullable;
import java.util.ArrayList;

public class MyMusicDBOpenHelper extends SQLiteOpenHelper {
    private Context context;
    //DB생성을 위한 생성자
    public MyMusicDBOpenHelper(@Nullable Context context, int version) {
        super(context, "myMusicTBL", null, version);
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    //테이블을 만드는 함수
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String query = "create table myMusicTBL("+
                "id VARCHAR(60) PRIMARY KEY,"+
                "artist VARCHAR(20),"+
                "title VARCHAR(20),"+
                "albumArt VARCHAR(20),"+
                "duration VARCHAR(20),"+
                "click INTEGER,"+
                "liked INTEGER);";
        sqLiteDatabase.execSQL(query);
    }
    //테이블을 삭제하는 함수
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        String query = "drop table if exists myMusicTBL;";
        sqLiteDatabase.execSQL(query);
        //콜백 함수 끼리는 서로 콜을 할 수 가 있다. 재생성을 위해 부른다.
        onCreate(sqLiteDatabase);
    }

    //DB에 음악리스트를 저장하는 함수
    public boolean insertMusicDatabase(){
        //db 열기
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        boolean returnValue = false;

        String[] data ={
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION};

        //String selection = MediaStore.Audio.Media.DATA+" like ?"
        // new String[]{"%Music1%"};
        //String selection1 = MediaStore.Audio.Media.IS_MUSIC + "!= 0";

        //"ASC" 오름차순 정렬
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                data,null,null,data[2]+" ASC");
        try {
            //db에 넣기
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    //음악안에 있는 데이터들 가져오기
                    String id = cursor.getString(cursor.getColumnIndex(data[0]));
                    String artist = cursor.getString(cursor.getColumnIndex(data[1]));
                    String title = cursor.getString(cursor.getColumnIndex(data[2]));
                    String albumArt = cursor.getString(cursor.getColumnIndex(data[3]));
                    String duration = cursor.getString(cursor.getColumnIndex(data[4]));
                    Log.d("DBinsert" ,id);
                    String query = "insert into myMusicTBL values(" +
                            "'" + id + "'," +
                            "'" + artist + "'," +
                            "'" + title + "'," +
                            "'" + albumArt + "'," +
                            "'" + duration + "'," +
                            0 + "," + 0 +");";
                    sqLiteDatabase.execSQL(query);
                }
            }
            returnValue = true;
            return returnValue;
        }catch (Exception e){
            Log.d("DBOpenHelper insert",e.getMessage());
            returnValue = false;
            return returnValue;
        }finally {
            //자원 반납
            sqLiteDatabase.close();
            cursor.close();
        }


    }
    //DB에 있는 음악 리스트를 RecycleView에 저장하는 함수
    public ArrayList<MusicData> setMusicDataRecycleView(){

        ArrayList<MusicData>musicData = new ArrayList<MusicData>();
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("select * from myMusicTBL",null);

        while (cursor.moveToNext()){
            String id = cursor.getString(0);
            String artist = cursor.getString(1);
            String title = cursor.getString(2);
            String albumArt = cursor.getString(3);
            String duration = cursor.getString(4);
            int click = cursor.getInt(5);
            int liked = cursor.getInt(6);

            MusicData music = new MusicData(id,artist,title,albumArt,duration,click,liked);
            musicData.add(music);
        }
        //자원 반납
        sqLiteDatabase.close();
        cursor.close();

        return musicData;
    }
    //DB에 있는 좋아요를 누른 리스트만 불러오는 함수
    public ArrayList<MusicData> setLikeMusicDataList(){
        ArrayList<MusicData>musicData = new ArrayList<MusicData>();
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        String query =  "select * from myMusicTBL where liked = 1;";
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.rawQuery(query,null);
            while (cursor.moveToNext()) {
                String id = cursor.getString(0);
                String artist = cursor.getString(1);
                String title = cursor.getString(2);
                String albumArt = cursor.getString(3);
                String duration = cursor.getString(4);
                int click = cursor.getInt(5);
                int liked = cursor.getInt(6);

                MusicData musicData1 = new MusicData(id, artist, title, albumArt, duration, click, liked);
                musicData.add(musicData1);
            }
        }catch (Exception e){
            Log.d("setLikeMusicDataList",e.getMessage());
            musicData = null;
        }finally {
            //자원 반납
            cursor.close();
            sqLiteDatabase.close();
        }


        return musicData;
    }

    //데이터 베이스에서 노래를 삭제하는 함수
    public void deleteFromDB(ArrayList<MusicData>musicData,int i){
        //db열기
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        //db에서 음악 삭제
        String query = "delete from myMusicTBL where id = '"+
                musicData.get(i).getId()+"';";
        sqLiteDatabase.execSQL(query);
        //자원반납
        sqLiteDatabase.close();
    }

    //좋아요 수 증감소에 관한 함수
    public void increaseOrDicreaseDatabase(ArrayList<MusicData>musicData,int i){
        //db열기
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        String query = "";
        if(musicData.get(i).getLiked() == 1){
            query = "update myMusicTBL set liked = "+musicData.get(i).getLiked()+" where id = '"+
                    musicData.get(i).getId()+"';";
        }else{
            query = "update myMusicTBL set liked = "+0+" where id = '"+
                    musicData.get(i).getId()+"';";
        }
        sqLiteDatabase.execSQL(query);
        sqLiteDatabase.close();
    }
    //카운트 수에 관한 함수
    public void increaseClickCount(ArrayList<MusicData>musicData,int i){
        //db열기
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        musicData.get(i).setClick(musicData.get(i).getClick()+1);
        String query = "update myMusicTBL set click = "+(musicData.get(i).getClick()+1)+" where id = '"+
                musicData.get(i).getId()+"';";
        sqLiteDatabase.execSQL(query);
        sqLiteDatabase.close();
    }
    /*public void allDelete(){
        String query  = "delete from myMusicTBL";
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.execSQL(query);
        sqLiteDatabase.close();
    }*/

    // sdCard 안의 음악을 검색한다
    public ArrayList<MusicData> findMusic() {
        ArrayList<MusicData> sdCardList = new ArrayList<>();


        String[] data = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION};

        String selection = MediaStore.Audio.Media.DATA + " like ? ";

        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                data, selection, new String[]{"%Music1%"}, data[2] + " ASC");

        if (cursor != null) {
            while (cursor.moveToNext()) {

                // 음악 데이터 가져오기
                String id = cursor.getString(cursor.getColumnIndex(data[0]));
                String artist = cursor.getString(cursor.getColumnIndex(data[1]));
                String title = cursor.getString(cursor.getColumnIndex(data[2]));
                String albumArt = cursor.getString(cursor.getColumnIndex(data[3]));
                String duration = cursor.getString(cursor.getColumnIndex(data[4]));

                MusicData mData = new MusicData(id, artist, title, albumArt, duration, 0, 0);

                sdCardList.add(mData);
            }
        }

        return sdCardList;
    }

    // DB Select
    public ArrayList<MusicData> selectMusicTbl() {

        ArrayList<MusicData> musicDBArrayList = new ArrayList<>();

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        // 쿼리문 입력하고 커서 리턴 받음
        Cursor cursor = sqLiteDatabase.rawQuery("select * from myMusicTBL;", null);

        while (cursor.moveToNext()) {
            MusicData musicData = new MusicData(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getInt(5),
                    cursor.getInt(6));

            musicDBArrayList.add(musicData);
        }

        cursor.close();
        sqLiteDatabase.close();

        return musicDBArrayList;
    }

    // sdcard에서 검색한 음악과 DB를 비교해서 중복되지 않은 플레이리스트를 리턴
    public ArrayList<MusicData> compareArrayList(){
        ArrayList<MusicData> sdCardList = findMusic();
        ArrayList<MusicData> dbList = setMusicDataRecycleView();

        // DB가 비었다면 sdcard리스트 리턴
        if(dbList.isEmpty()){
            return sdCardList;
        }

        // DB가 이미 sdcard 정보를 가지고 있다면 DB리스트를 리턴
        if(dbList.containsAll(sdCardList)){
            return dbList;
        }

        // 두 리스트를 비교후 중복되지 않은 값을 DB리스트에 추가후 리턴
        int size = dbList.size();
        for (int i = 0; i < size; ++i) {
            if (dbList.contains(sdCardList.get(i))) {
                continue;
            }
            dbList.add(sdCardList.get(i));
            ++size;
        }

        return dbList;
    }
}
