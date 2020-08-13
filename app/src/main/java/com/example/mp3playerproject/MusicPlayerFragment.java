package com.example.mp3playerproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.icu.text.SimpleDateFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class MusicPlayerFragment extends Fragment implements View.OnClickListener {
    private ImageView imgFragment;
    private TextView tvFragCount,tvFragMusicName,tvFragArtist,musicCurrent,musicDuration;
    private SeekBar seekBar;
    private ImageButton ibLike,ibPrevious,ibStart,ibNext;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private ArrayList<MusicData>arrayList;
    private MainActivity mainActivity;
    private RecyclerMusicListAdapter recyclerMusicListAdapter;
    private MyMusicDBOpenHelper myMusicDBOpenHelper;
    private int index;
    private MusicData data;
    private int num;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mainActivity = (MainActivity)getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.mainActivity = null;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.musicplayfragment1,container,false);
        //아이디 찾는 함수
        findViewByIdFunction(view);
        //버튼 클릭에 대한 함수
        btnClickMethod();
        //시크바 변경에 관한 함수
        seekBarChangeMethod();
        //처음 켰을 경우 랜덤한 음악을 보여주는 함수
        selectFirstMusicData();


        return view;
    }
    //마지막 곡 정보 받아서 설정하는 함수
    private void setFirstsetting() {

    }

    //아이디 찾는 함수
    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void findViewByIdFunction(View view) {
        imgFragment = view.findViewById(R.id.imgFragment);
        tvFragCount = view.findViewById(R.id.tvFragCount);
        tvFragMusicName = view.findViewById(R.id.tvFragMusicName);
        tvFragArtist = view.findViewById(R.id.tvFragArtist);
        musicCurrent = view.findViewById(R.id.musicCurrent);
        musicDuration = view.findViewById(R.id.musicDuration);
        seekBar = view.findViewById(R.id.seekBar);
        ibLike = view.findViewById(R.id.ibLike);
        ibPrevious = view.findViewById(R.id.ibPrevious);
        ibStart = view.findViewById(R.id.ibStart);
        ibNext = view.findViewById(R.id.ibNext);

    }
    //버튼 클릭에 대한 함수
    private void btnClickMethod() {
        ibStart.setOnClickListener(this);
        ibPrevious.setOnClickListener(this);
        ibNext.setOnClickListener(this);
        ibLike.setOnClickListener(this);
    }

    //버튼 클릭 이벤트처리 함수
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ibStart :
                if(ibStart.isActivated()){
                    mediaPlayer.pause();
                    ibStart.setActivated(false);
                }else{
                    mediaPlayer.start();
                    ibStart.setActivated(true);
                    setSeekBarThread();
                }
                break;
            case R.id.ibPrevious :
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                if(num != -1){
                    index = num;
                }
                    try {
                        if(index == 0){
                            index = mainActivity.getArrayList().size();
                        }
                        index--;
                        selectedMusicPlayAndScreenSetting(index);

                    } catch (Exception e) {
                        Log.d("ubPrevious",e.getMessage());
                    }

                break;
            case R.id.ibNext :
                if(num != -1){
                    index = num;
                }
                try {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                    if(index == mainActivity.getArrayList().size()-1){
                        index= -1;
                    }
                    index++;
                    selectedMusicPlayAndScreenSetting(index);
                } catch (Exception e) {
                    Log.d("ibNext",e.getMessage());
                }
                break;
            case R.id.ibLike :
                myMusicDBOpenHelper = mainActivity.getMyMusicDBOpenHelper();

                if(ibLike.isActivated()){
                    Toast.makeText(mainActivity,"좋아요 해제!",Toast.LENGTH_SHORT).show();
                    Toast.makeText(mainActivity,"좋아요 목록에서 삭제완료",Toast.LENGTH_SHORT).show();
                    //하트 버튼에 관한 DB업데이트 및 무효화영역처리에 관한 함수
                    ibLikeUpdateLiked(false,0);
                }else {
                    Toast.makeText(mainActivity,"좋아요 !",Toast.LENGTH_SHORT).show();
                    Toast.makeText(mainActivity,"좋아요 목록에 추가완료",Toast.LENGTH_SHORT).show();
                    //하트 버튼에 관한 DB업데이트 및 무효화영역처리에 관한 함수
                    ibLikeUpdateLiked(true,1);
                }

                break;
            default:break;
        }
    }
    //시크바 변경에 관한 함수
    private void seekBarChangeMethod() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b){
                    mediaPlayer.seekTo(i);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


    //선택된 음악재생및 화면 처리에 관한 함수
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void selectedMusicPlayAndScreenSetting(final int position) {
        num = -1;
        mediaPlayer.stop();
        mediaPlayer.reset();
        index = position;
        final ArrayList<MusicData> musicData = mainActivity.getArrayList();
        data = musicData.get(position);
        recyclerMusicListAdapter = new RecyclerMusicListAdapter(mainActivity,musicData);
        //내가 원하는 형식으로 보여주겠다.
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");

        Bitmap bitmap = recyclerMusicListAdapter.getAlbumImg(mainActivity,Integer.parseInt(data.getAlbumArt()),200);
        if(bitmap != null){
            imgFragment.setImageBitmap(bitmap);
        }
        tvFragMusicName.setText(data.getTitle());
        tvFragArtist.setText(data.getArtist());
        tvFragCount.setText(String.valueOf(data.getClick()));
        musicDuration.setText(simpleDateFormat.format(Integer.parseInt(data.getDuration())));

        if(data.getLiked() ==1){
            ibLike.setActivated(true);
        }else{
            ibLike.setActivated(false);
        }
        //긴 음악이름으로 좌우로 움직여 보여주기
        tvFragMusicName.setSelected(true);
        Uri musicURI = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,data.getId());
        try {
            mediaPlayer.setDataSource(mainActivity,musicURI);
            mediaPlayer.prepare();
            mediaPlayer.start();
            seekBar.setProgress(0);
            seekBar.setMax(Integer.parseInt(data.getDuration()));
            ibStart.setActivated(true);

            setSeekBarThread();
            //재생이 끝났을 때 이벤트 처리 함수
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    myMusicDBOpenHelper = mainActivity.getMyMusicDBOpenHelper();
                    myMusicDBOpenHelper.increaseClickCount(mainActivity.getArrayList(),position);
                    data.setClick(data.getClick());
                    ibNext.callOnClick();
                    mainActivity.getLikemusicListAdapter().notifyDataSetChanged();
                    mainActivity.getMusicListAdapter().notifyDataSetChanged();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //좋아요 리스트 노래재생 함수
    //선택된 음악재생및 화면 처리에 관한 함수
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void selectedLikeMusicPlayAndScreenSetting(final int position) {
        mediaPlayer.stop();
        mediaPlayer.reset();
        index = position;
        final ArrayList<MusicData> musicData = mainActivity.getArrayLikeList();
        data = musicData.get(position);
        recyclerMusicListAdapter = new RecyclerMusicListAdapter(mainActivity,musicData);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");

        Bitmap bitmap = recyclerMusicListAdapter.getAlbumImg(mainActivity,Integer.parseInt(data.getAlbumArt()),200);
        if(bitmap != null){
            imgFragment.setImageBitmap(bitmap);
        }
        tvFragMusicName.setText(data.getTitle());
        tvFragArtist.setText(data.getArtist());
        tvFragCount.setText(String.valueOf(data.getClick()));
        musicDuration.setText(simpleDateFormat.format(Integer.parseInt(data.getDuration())));

        if(data.getLiked() ==1){
            ibLike.setActivated(true);
        }else{
            ibLike.setActivated(false);
        }
        Uri musicURI = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,data.getId());
        try {
            mediaPlayer.setDataSource(mainActivity,musicURI);
            mediaPlayer.prepare();
            mediaPlayer.start();
            seekBar.setProgress(0);
            seekBar.setMax(Integer.parseInt(data.getDuration()));
            ibStart.setActivated(true);

            setSeekBarThread();
            //재생이 끝났을 때 이벤트 처리 함수
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    myMusicDBOpenHelper = mainActivity.getMyMusicDBOpenHelper();
                    myMusicDBOpenHelper.increaseClickCount(mainActivity.getArrayList(),position);
                    data.setClick(data.getClick());
                    ibNext.callOnClick();
                    mainActivity.getLikemusicListAdapter().notifyDataSetChanged();
                    mainActivity.getMusicListAdapter().notifyDataSetChanged();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //시크바 스레드 에 관한 함수
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setSeekBarThread(){
        Thread thread = new Thread(new Runnable() {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");

            @Override
            public void run() {
                while(mediaPlayer.isPlaying()){
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            musicCurrent.setText(simpleDateFormat.format(mediaPlayer.getCurrentPosition()));
                        }
                    });
                    SystemClock.sleep(100);
                }
            }
        });
        thread.start();
    }
    //하트 버튼에 관한 DB업데이트 및 무효화영역처리에 관한 함수
    private void ibLikeUpdateLiked(boolean b,int i){
        ibLike.setActivated(b);
        ArrayList<MusicData>musicData = mainActivity.getArrayList();
        musicData.get(index).setLiked(i);
        if(i == 1){
            myMusicDBOpenHelper.increaseOrDicreaseDatabase(mainActivity.getArrayList(),index);
            mainActivity.getArrayLikeList().add(musicData.get(index));
            mainActivity.setArrayLikeList(mainActivity.getArrayLikeList());
        }else if(i == 0){
            myMusicDBOpenHelper.increaseOrDicreaseDatabase(mainActivity.getArrayList(),index);
            mainActivity.getArrayLikeList().remove(musicData.get(index));
            mainActivity.setArrayLikeList(mainActivity.getArrayLikeList());
        }
        mainActivity.getLikemusicListAdapter().notifyDataSetChanged();
    }
    //처음 켰을 경우 랜덤한 음악을 보여주는 함수
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void selectFirstMusicData(){
        ArrayList<MusicData> musicData = mainActivity.getArrayList();
        Random rd = new Random();
        num = rd.nextInt(musicData.size()-1);
        data = musicData.get(num);
        recyclerMusicListAdapter = new RecyclerMusicListAdapter(mainActivity,musicData);
        //내가 원하는 형식으로 보여주겠다.
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");

        Bitmap bitmap = recyclerMusicListAdapter.getAlbumImg(mainActivity,Integer.parseInt(data.getAlbumArt()),200);
        if(bitmap != null){
            imgFragment.setImageBitmap(bitmap);
        }
        tvFragMusicName.setText(data.getTitle());
        tvFragArtist.setText(data.getArtist());
        tvFragCount.setText(String.valueOf(data.getClick()));
        musicDuration.setText(simpleDateFormat.format(Integer.parseInt(data.getDuration())));

        if(data.getLiked() ==1){
            ibLike.setActivated(true);
        }else{
            ibLike.setActivated(false);
        }
        //긴 음악이름으로 좌우로 움직여 보여주기
        tvFragMusicName.setSelected(true);
        if(data.getLiked() ==1){
            ibLike.setActivated(true);
        }else{
            ibLike.setActivated(false);
        }
        Uri musicURI = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,data.getId());
        try {
            mediaPlayer.setDataSource(mainActivity,musicURI);
            mediaPlayer.prepare();
            seekBar.setProgress(0);
            seekBar.setMax(Integer.parseInt(data.getDuration()));
            ibStart.setActivated(false);

            setSeekBarThread();
            //재생이 끝났을 때 이벤트 처리 함수
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    myMusicDBOpenHelper = mainActivity.getMyMusicDBOpenHelper();
                    myMusicDBOpenHelper.increaseClickCount(mainActivity.getArrayList(),num);
                    data.setClick(data.getClick());
                    ibNext.callOnClick();
                    mainActivity.getLikemusicListAdapter().notifyDataSetChanged();
                    mainActivity.getMusicListAdapter().notifyDataSetChanged();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //get,set

    public MusicData getData() {
        return data;
    }

    public void setData(MusicData data) {
        this.data = data;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
