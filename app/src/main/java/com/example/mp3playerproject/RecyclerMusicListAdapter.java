package com.example.mp3playerproject;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class RecyclerMusicListAdapter extends RecyclerView.Adapter<RecyclerMusicListAdapter.CustomViewHolder> {
    private Context context;
    private ArrayList<MusicData> dataArrayList;
    // 리스너 객체 참조를 저장하는 변수
    private OnItemClickListener itemClickListener = null;
    private int adapterPosition;



    public RecyclerMusicListAdapter(Context context,ArrayList<MusicData>arrayList) {
        this.context = context;
        this.dataArrayList = arrayList;
    }

    @NonNull
    @Override
    public RecyclerMusicListAdapter.CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.music_data_layout,parent,false);

        RecyclerMusicListAdapter.CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerMusicListAdapter.CustomViewHolder holder, final int position) {
        adapterPosition = position;
        //앨범이미지를 비트맵으로 만들기
        Bitmap bitmap = getAlbumImg(context,Integer.parseInt(dataArrayList.get(position).getAlbumArt()),200);
        if(bitmap != null){
            holder.imgAlbum.setImageBitmap(bitmap);
        }
        holder.tvMusicName.setText(dataArrayList.get(position).getTitle());
        holder.tvArtist.setText(dataArrayList.get(position).getArtist());
        holder.tvCount.setText(String.valueOf(dataArrayList.get(position).getClick()));

    }
    // 커스텀 리스너 인터페이스
    public interface OnItemClickListener{
        void onItemClick(View view,int pos);
    }


    /*@NonNull
    @Override
    public RecyclerMusicListAdapter.CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.music_data_layout,parent,false);

        RecyclerMusicListAdapter.CustomViewHolder viewHolder = new CustomViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        MusicData musicData = dataArrayList.get(position);

        //앨범이미지를 비트맵으로 만들기
        Bitmap bitmap = getAlbumImg(context,Integer.parseInt(musicData.getAlbumArt()),200);
        if(bitmap != null){
            holder.imgAlbum.setImageBitmap(bitmap);
        }
        holder.tvMusicName.setText(musicData.getTitle());
        holder.tvArtist.setText(musicData.getArtist());
        holder.tvCount.setText(String.valueOf(musicData.getClick()));
    }*/

    @Override
    public int getItemCount() {
        return dataArrayList != null ? dataArrayList.size() : 0;
        //dataArrayList != null ? dataArrayList.size() : 0
    }
    public Bitmap getAlbumImg(Context context, int albumArt, int imgMaxSize){
        BitmapFactory.Options options = new BitmapFactory.Options();
        /*컨텐트 프로바이더(Content Provider)는 앱 간의 데이터 공유를 위해 사용됨.
        특정 앱이 다른 앱의 데이터를 직접 접근해서 사용할 수 없기 때문에
        무조건 컨텐트 프로바이더를 통해 다른 앱의 데이터를 사용해야만 한다.
        다른 앱의 데이터를 사용하고자 하는 앱에서는 URI를 이용하여 컨텐트 리졸버(Content Resolver)를 통해 다른 앱의 컨텐트 프로바이더에게 데이터를 요청하게 되는데
        요청받은 컨텐트 프로바이더는 URI를 확인하고 내부에서 데이터를 꺼내어 컨텐트 리졸버에게 전달한다.
        */
        ContentResolver contentResolver = context.getContentResolver();
        //앨범아트는 uri 제공 X 직접 선언해줘야 한다.
        Uri uri = Uri.parse("content://media/external/audio/albumart/"+albumArt);
        if(uri != null){
            ParcelFileDescriptor fd = null;
            try {
                fd = contentResolver.openFileDescriptor(uri,"r");
                options.inJustDecodeBounds = true;
                //true면 비트맵 객체에 메모리를 할당하지 않아서 비트맵을 반환하지 않음.
                //다만 option fields는 값이 채워지기 때문에 로드하려는 이미지의 크기를 포함한 정보들을 얻어올 수 있다.
                //BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(),null,options);
                int scale = 0;
                if(options.outHeight > imgMaxSize || options.outWidth > imgMaxSize){
                    scale = (int) Math.pow(2, (int) Math.round(Math.log(imgMaxSize / (double) Math.max(options.outHeight, options.outWidth)) / Math.log(0.5)));
                }
                options.inJustDecodeBounds = false; //true 면 비트맵을 만들지 않고 해당이미지의 가로, 세로, Mime type등의 정보만 가져옴
                options.inSampleSize = scale; //이미지의 원본사이지를 설정된 크기로 변경

                Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(),null,options);
                if(bitmap != null){
                    //좀 더 정확히 이미지 크기를 설정
                    if(options.outWidth != imgMaxSize || options.outHeight !=imgMaxSize){
                        Bitmap btp = Bitmap.createScaledBitmap(bitmap,imgMaxSize,imgMaxSize,true);
                        bitmap.recycle();
                        bitmap = btp;
                    }
                }
                return bitmap;

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }finally {
                //자원 반납
                if(fd != null){
                    try {
                        fd.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }
    //get,set
    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public ArrayList<MusicData> getDataArrayList() {
        return dataArrayList;
    }

    public void setDataArrayList(ArrayList<MusicData> dataArrayList) {
        this.dataArrayList = dataArrayList;
    }
    // OnItemClickListener 객체 참조를 어댑터에 전달하는 메서드
    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public int getAdapterPosition() {
        return adapterPosition;
    }

    public void setAdapterPosition(int adapterPosition) {
        this.adapterPosition = adapterPosition;
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        public ImageView imgAlbum;
        public TextView tvMusicName;
        public TextView tvArtist;
        public TextView tvCount;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            this.imgAlbum  = itemView.findViewById(R.id.d_imgAlbum);
            this.tvMusicName  = itemView.findViewById(R.id.d_tvMusicName);
            this.tvArtist  = itemView.findViewById(R.id.d_tvArtist);
            this.tvCount  = itemView.findViewById(R.id.d_tvCount);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = getAdapterPosition();
                    if(pos != RecyclerView.NO_POSITION){
                        itemClickListener.onItemClick(view,pos);
                    }
                }
            });
        }


    }

}
