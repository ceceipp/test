```java
package com.lc.musicplayer.tools;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Toast;

import com.lc.musicplayer.MyApplication;
import com.lc.musicplayer.R;
import com.lc.musicplayer.service.MusicService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 *  @  songsPosition  是指传入的Songs数组中选中的序号
 *  @  usingPosition 是指正在播放的Position
 *  @ usingPositionList 正在使用的播放列表与传入的Songs数组的列表的映射
 *  @ oriUsingPositionList 是UsingPositionList设置前的上一个list, 也可以理解成
 *      外部传进来的映射list
 *  @ songIsSelectedList 是edit界面的List的item是否被选中的list
 *  @ sameStringSingleList 是sameStringSingleList传进来的数组,
 *      key序号是songList的排序, value是对应oriSongList的歌曲ID,
 *      可以根据ID重新设置usingPositionList
* */

public  class   Player {
    public  int order_Mode = Data.Order_Repeat_Playlist;
    private  List<Song> songs  ;
    private int usingPosition;
    private int usingPositionId;
    private List<Integer> usingPositionList = new ArrayList<>();
    private List<Integer> oriUsingPositionList = new ArrayList<>();
    public  MediaPlayer mediaPlayer = new MediaPlayer();
    public boolean musicInfoNeedUpdate=true;
    private List<Boolean> songIsSelectedList;
    private List<Integer> sameStringSingleList;

    public Player(){
        usingPosition=0;
        usingPositionId=0;
        musicInfoNeedUpdate=false;
    }
    /**
     * @param songs 就是输入的原始歌单(一定是全部歌曲的歌单,
     *              喜爱歌曲的歌单是要根据这个原始歌单来映射的)
     * **/

    public Player(List<Song> songs){
        if (songs==null){
            int i =0;
            this.songs=new ArrayList<>();
            oriUsingPositionList = usingPositionList;
            usingPositionId = 0;
            musicInfoNeedUpdate=true;
        }
        else {
            int i =0;
            this.songs=songs;
            for (i=0; i<this.songs.size(); i++)
                usingPositionList.add(i,i);
            //playSong(songs.get(usingPositionList.get(usingPosition)).getPath());
            oriUsingPositionList = usingPositionList;
            if (usingPositionList.size()-10>=0){
                playSong(songs.get(usingPositionList.size()-10).getPath());
                usingPositionId = usingPositionList.size()-10;
            }
            else {
                playSong(songs.get(0).getPath());
                usingPositionId =0;
            }
            mediaPlayer.pause();
            musicInfoNeedUpdate=true;
        }
    }

    public void playSong(String path){
        if (path==null){
            //nextSong();
            Toast.makeText(MyApplication.getContext(),"No this song",Toast.LENGTH_LONG).show();
        }
        if (usingPosition<0)
            mediaPlayer.stop();
        try{
            mediaPlayer.reset();
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            mediaPlayer.start();
            usingPositionId = usingPositionList.get(usingPosition);
        }
        catch (IOException e){
            e.printStackTrace();nextSong();
        }
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                switch (order_Mode){
                    case Data.Order_Repeat_Track: mediaPlayer.start(); break;
                    case Data.Order_Repeat_Playlist: nextSong(); break;
                    case Data.Order_Shuffle_Playlist: nextSong(); break;
                    case Data.Order_One_Track: mediaPlayer.pause(); break;
                    case Data.Order_Random: usingPosition = randomNext(usingPositionList.size()); nextSong(); break;
                    default:break;
                }
            }
        });
        musicInfoNeedUpdate=true;
    }

    public void startOrPause(){
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            musicInfoNeedUpdate=true;
        }
        else{
            if (mediaPlayer==null)
                //原来是 playSong(songs.get(0).getPath());
                playSong(songs.get(usingPositionList.get(0)).getPath());
            else {
                mediaPlayer.start();
                musicInfoNeedUpdate = true;
            }
        }
    }

    public void stop(){
        if (mediaPlayer!=null)
            mediaPlayer.stop();
        try{
            mediaPlayer.prepare();
            mediaPlayer.seekTo(0);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        musicInfoNeedUpdate=true;
    }

    public void nextSong(){
        if (order_Mode==Data.Order_Random)
            usingPosition = randomNext(usingPositionList.size());
        usingPosition++;
        if(mediaPlayer!=null && usingPosition < usingPositionList.size()) {
            mediaPlayer.stop();
            try{
                playSong(songs.get(usingPositionList.get(usingPosition)).getPath());
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        else if (usingPosition>=usingPositionList.size() && mediaPlayer!=null){
            mediaPlayer.stop();
            try{
                usingPosition=0;
                playSong(songs.get(usingPositionList.get(usingPosition)).getPath());
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void preSong(){
        if(mediaPlayer!=null && usingPosition>0) {
            mediaPlayer.stop();
            try{
                usingPosition--;
                playSong(songs.get(usingPositionList.get(usingPosition)).getPath());
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        else if (usingPosition<=0 && mediaPlayer!=null){
            mediaPlayer.stop();
            try{
                usingPosition=usingPositionList.size()-1;
                playSong(songs.get(usingPositionList.get(usingPosition)).getPath());
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void seekTo(int mSec){
        if (mediaPlayer!=null){
            try{
                mediaPlayer.seekTo(mSec);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        musicInfoNeedUpdate=true;
    }
    public int seekBarPercentage(){
        //假设seekBar长度为int200
        int percentage = 200*mediaPlayer.getCurrentPosition();
        return (int)  (    (float) percentage/songs.get(usingPositionList.get(usingPosition)).getDurationMsec()    ) ;
    }

    public void change_Order_Mode(){
        order_Mode++;
        order_Mode = order_Mode%5;
        if (order_Mode==Data.Order_Shuffle_Playlist){
             usingPositionList = getRandomList(usingPositionList.size(),oriUsingPositionList);
            //usingPosition=usingPositionList.indexOf(usingPosition)  ;
            usingPosition = usingPositionList.indexOf(getUsingPositionId());
        }
        if (order_Mode!=Data.Order_Shuffle_Playlist && order_Mode!=Data.Order_Random){
            //usingPosition=getUsingPositionId()  ;
            usingPositionList = oriUsingPositionList;
            usingPosition=usingPositionList.indexOf(getUsingPositionId());
        }
        musicInfoNeedUpdate=true;
    }
    public static int randomNext(int num){
        Random random = new Random();
        num = random.nextInt(num);
        return num;
    }
    public void setUsingPositionList(List<Integer> usingPositionList) {
        oriUsingPositionList = usingPositionList;
//        if (order_Mode==Data.Order_Shuffle_Playlist)
//            this.usingPositionList= getRandomList(usingPositionList.size(), usingPositionList);
        this.usingPositionList = usingPositionList;
    }
    public List<Integer> getUsingPositionList() {
        return usingPositionList;
    }
    public int getUsingPositionId() {
        //return usingPositionList.get(usingPosition);
        //如果歌曲的ID改变了, 就不能用此方法了
        if (usingPosition>=usingPositionList.size())
            return 1;
        return usingPositionId;
    }
    /**
     * 此句同上面等价(在歌单导入无误的情况下),但会出现UI更新不正确,
     * 因为如果不执行setUsingPositionId(clickSongPosition)的话;
     * UI更新是根据usingPosition来决定的, 虽然下面的方法能播放歌曲
     * 但是UI还是原来的UI, 所以这个firstClickListItem函数我打算
     * 写多一个方法相同但加多一个参数的函数, 加的参数就是歌曲列表的参数
     * playSong(songs.get( clickSongPosition).getPath());
     * **/
    public void firstClickListItem(int clickSongPosition){
        setUsingPositionId(clickSongPosition);
        playSong(songs.get(usingPositionList.get(usingPosition)).getPath());
    }
    public void firstTapFromList(int clickSongPosition, List<Integer> list){
        setUsingPositionList( list );
        setUsingPositionId(clickSongPosition);
        playSong(songs.get(usingPositionList.get(usingPosition)).getPath());
    }
    /**usingPosition<0意味着不是从第一个Activity点击歌曲曲目进去的,所以不用执行firstClickListItem()
     * 原来是this.usingPosition=usingPositionList.indexOf(usingPosition);但是在换了歌曲曲单后会
     * 出现Id为1000的歌曲, 原来的曲单index为4, 新曲单长度为3,那么就会导致换成了新曲单后set1000,
     * 然后index得到4, 但是曲单只有3, 内存溢出
     **/
    public void setUsingPositionId(int usingPosition) {
        if (usingPosition<0) {
            this.usingPosition = usingPositionList.size() - 1;
            Toast.makeText(MyApplication.getContext(),
                    "No this song at playlist queue. ",Toast.LENGTH_SHORT).show();
        }
        else{
            if ( usingPositionList.contains(usingPosition) )
                this.usingPosition=usingPositionList.indexOf(usingPosition);
            else {
                this.usingPosition = usingPositionList.size() - 1;
                Toast.makeText(MyApplication.getContext(),
                        "No this song at playlist queue. ",Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void setSongs(List<Song> songs){
        this.songs =songs;
    }
    public List<Song> getSongs(){return this.songs;}

    public  static int fileFormatdDetect(String string){
        Map<String, Integer> map = new HashMap<>();
        map.put(".mp3", R.drawable.mp3);
        map.put(".wav",R.drawable.wav);
        map.put(".m4a" , R.drawable.m4a);
        map.put(".ape", R.drawable.ape);
        map.put("flac", R.drawable.flac);
        string = string.toLowerCase();
        string =string.substring(string.length()-4);
        if (map.get(string)==null)
            return R.drawable.other;
        return map.get(string);
    }

    public  void findSongWithTitle(String string){
    for (int i=0;i<songs.size();i++){
        if(songs.get(i).getSong().contains(string)) {
            firstClickListItem(i);
            break;
        }
        else {}
    }
    Toast.makeText(MyApplication.getContext(),
            "No this search", Toast.LENGTH_SHORT).show();
}

/**
 * 为了提高复用性, 下列4个方法特意做成相似的样子, 以便搭配Adatper, 而且经过多次改良,
 * 这几个方法是始祖方法的1/5耗时哈哈哈
 * **/
    public static List<SameStringIdList> idToSameAlbumConvert(List<Song> songList){
        Set<String> stringSet =new android.support.v4.util.ArraySet<>();
        for (int i=0;i<songList.size();i++){
            stringSet.add(songList.get(i).getAlbum());
        }

        ArrayList<String> stringList = new ArrayList(stringSet);
        Collections.sort(stringList);
        ArrayList<SameStringIdList> sameStringIdLists=new ArrayList<>();
        for (int num=0; num<stringList.size(); num++){
            sameStringIdLists.add(new SameStringIdList(stringList.get(num)));
        }
        for ( int i =0;i<songList.size();i++){
            for ( int j = 0; j<stringList.size();j++){
                if ( stringList.get(j).contentEquals( songList.get(i).getAlbum()  ))
                { sameStringIdLists.get(j).getList().add(i);  break; }
            }
        }
        //Collections.sort(sameStringIdLists);
        stringSet = null;
        stringList=null;
        return sameStringIdLists;
    }
    public static List<SameStringIdList> idToSameSingerConvert(List<Song> songList){
        Set<String> stringSet =new android.support.v4.util.ArraySet<>();
        for (int i=0;i<songList.size();i++){
            stringSet.add(songList.get(i).getSinger());
        }

        ArrayList<String> stringList = new ArrayList(stringSet);
        Collections.sort(stringList);
        ArrayList<SameStringIdList> sameStringIdLists=new ArrayList<>();
        for (int num=0; num<stringList.size(); num++){
            sameStringIdLists.add(new SameStringIdList(stringList.get(num)));
        }
        for ( int i =0;i<songList.size();i++){
            for ( int j = 0; j<stringList.size();j++){
                if ( stringList.get(j).contentEquals( songList.get(i).getSinger()  ))
                { sameStringIdLists.get(j).getList().add(i);  break; }
            }
        }
        //Collections.sort(sameStringIdLists);
        stringSet = null;
        stringList=null;
        return sameStringIdLists;
    }
    /**
    * 注释掉的三句话是避免String中没有"/"导致出错, 除了注释掉的三个方法外,
     * 还有一个简单粗暴的方法, 直接在裁剪前直接加入4个"/",能运行
    * */
    public static List<SameStringIdList> idToSamePathConvert(List<Song> songList){
        ArrayList<String> arrayList=new ArrayList<>();
        for (int i =0; i<songList.size(); i++){
            //if (!songList.get(i).getPath().contains("/")){ arrayList.add(songList.get(i).getPath());       break; }
            String oriString = new String(songList.get(i).getPath());
             oriString = " //// "+oriString;
            int cutEnd = oriString.lastIndexOf("/");
            int cut = cutEnd;
                String stringCache =oriString.substring(0,cut);
            //if (!stringCache.contains("/")){ arrayList.add(songList.get(i).getPath());       break; }
                cut = stringCache.lastIndexOf("/");
                stringCache =oriString.substring(0,cut);
            int cutBegin = stringCache.lastIndexOf("/");
            //if (!stringCache.contains("/")){ arrayList.add(songList.get(i).getPath());       break; }
            String string=oriString.substring(cutBegin+1,cutEnd);
            arrayList.add(string);
        }

        Set<String> pathSet = new android.support.v4.util.ArraySet<>(arrayList);
        ArrayList<String> stringsList = new ArrayList(pathSet);

        Collections.sort(stringsList);
        ArrayList<SameStringIdList> sameStringIdLists=new ArrayList<>();
        for (int num=0; num<stringsList.size(); num++){
            sameStringIdLists.add(new SameStringIdList(stringsList.get(num)));
        }
        for ( int i =0;i<arrayList.size();i++){
            for ( int j = 0; j<stringsList.size();j++){
                if ( stringsList.get(j).contentEquals( arrayList.get(i) ))
                { sameStringIdLists.get(j).getList().add(i);  break; }
            }
        }
        //Collections.sort(sameStringIdLists);
        arrayList=null;
        stringsList=null;
        return sameStringIdLists;
    }
    public static void findPath(List<Song> songs){
        ArrayList<String> arrayList=new ArrayList<>();
        String string = null;
        int cut =0;
        for (int i =0; i<songs.size(); i++){
            if (!songs.get(i).getPath().contains("/"))
                break;
            cut = songs.get(i).getPath().lastIndexOf("/");
            string=songs.get(i).getPath().substring(0,cut);
            arrayList.add(string);
        }
        Set<String> pathSet = new android.support.v4.util.ArraySet<>(arrayList);
    }
    public static String allDurationTime( List<SameStringIdList> sameList,int position,List<Song> oriList){
        int timeMsec = 0;
        for (int i=0; i < sameList.get(position).getList().size(); i++  ){
            timeMsec = timeMsec  +
                    oriList.get( (int) (sameList.get(position).getList().get(i))  ).getDurationMsec();
        }
        return AudioUtils.formatTime(timeMsec);
    }
    public static List<Song> singleListToSongList(List<Integer> singleList, List<Song> fullSongList){
        List<Song> songList = new ArrayList<>();
        for (int i=0;i<singleList.size();i++) {
            songList.add(fullSongList.get( singleList.get(i) ));
        }
        return songList;
    }

    public static List<Integer>  sameStringListToList(List<SameStringIdList> sameStringIdLists,int position){
        ArrayList<Integer> singleList = new ArrayList<>();
        singleList = sameStringIdLists.get(position).getList();
        Collections.sort(singleList);
        return singleList;
    }
    public static List<Integer>  sameStringListToList( SameStringIdList  sameStringIdList){
        ArrayList<Integer> singleList = new ArrayList<>();
        singleList =sameStringIdList.getList();
        Collections.sort(singleList);
        return singleList;
    }

    public List<SameStringIdList> idStringSving(List<Song> songList) {
        //此方法是上面好几个方法的始祖, 也可以用, 但是比上面的几个方法要慢五倍
        List<SameStringIdList> forReturnList = new ArrayList<>();
        ArrayList<String> sameStringList = new ArrayList<>();
        ArrayList list=new ArrayList();
        for (  int i = 0; i < songList.size(); i++) {
            for (int  j = 0; j < songList.size(); j++) {
                if (songList.get(i).getAlbum().contentEquals(songList.get(j).getAlbum()))
                    list.add(j);
            }
            if (!sameStringList.contains(songList.get(i).getAlbum())){
                forReturnList.add(new SameStringIdList(songList.get(i).getAlbum(),list));
                sameStringList.add(songList.get(i).getAlbum());
            }
            list=new ArrayList();
        }
        sameStringList=null;list=null;
        return forReturnList;
    }
    /**Player大图片的加载方法, 一般只加载一张图片时采用**/
    public static Bitmap loadingCover(String musicFilePath) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(musicFilePath);
        byte[] picture = null;
        Bitmap bitmap = null;
        if (mediaMetadataRetriever.getEmbeddedPicture() != null) {
            picture = mediaMetadataRetriever.getEmbeddedPicture();
            bitmap = BitmapFactory.decodeByteArray(picture, 0, picture.length);
        } else {
//            bitmap = BitmapFactory.decodeResource(
//                    MyApplication.getContext().getResources(), R.drawable.nonepic2);
            bitmap = BitmapFactory.decodeResource(
                    MyApplication.getContext().getResources(), Player.fileFormatdDetect(musicFilePath));
        }
        return bitmap;
        //return compressImage(bitmap,100);
    }

    /**
     * 质量压缩方法
     * @param image
     * 有更好的方法在下面
     */
    public static Bitmap compressImage(Bitmap image,int quality) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        image.compress(Bitmap.CompressFormat.JPEG, quality, baos);
//        int options = 90;
//        // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
//        while (baos.toByteArray().length / 1024 > 100) {
//            // 重置baos即清空baos
//            baos.reset();
//            // 这里压缩options%，把压缩后的数据存放到baos中
//            image.compress(Bitmap.CompressFormat.JPEG, options, baos);
//            // 每次都减少10
//            options -= 10;
//        }
//        // 把压缩后的数据baos存放到ByteArrayInputStream中

        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        // 把ByteArrayInputStream数据生成图片
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
        return bitmap;
    }

    public void setSongIsSelectedList(List<Boolean> songIsSelectedList){
        if (this.songIsSelectedList==null){
            this.songIsSelectedList = new ArrayList<>();
            for (int i=0; i<songIsSelectedList.size();i++){
                this.songIsSelectedList.add(false);
            }
        }
        this.songIsSelectedList=songIsSelectedList;
    }
    public List<Boolean> getSongIsSelectedList(){
        return this.songIsSelectedList;
    }
    public void setSameStringSingleList(List<Integer> sameStringSingleList) {
        this.sameStringSingleList = sameStringSingleList;
    }
    public List<Integer> getSameStringSingleList() {
        return sameStringSingleList;
    }

    public static List<Integer> getOrderList(int num){
        List<Integer> list = new ArrayList<>();
        for (int i=0; i<num; i++)
            list.add(i,i);
        return list;
    }

    public static List<Integer> getRandomList(int num, List<Integer> oriList){
        //1.获取scope范围内的所有数值，并存到数组中
        int[] randomArray=new int[num];
        for(int i=0;i<randomArray.length;i++){
            randomArray[i]=i;
        }

        //2.从数组random中取数据，取过后的数改为-1
        int[] numArray=new int[num];//存储num个随机数
        int i=0;
        while(i<numArray.length){
            int index=(int)(Math.random()*num);
            if(randomArray[index]!=-1){
                numArray[i]=randomArray[index];
                randomArray[index]=-1;
                i++;
            }
        }
        ArrayList<Integer> arrayList = new ArrayList<>();
        for (int j=0;j<num;j++)
            arrayList.add(oriList.get(numArray[j]));
        return arrayList;
    }
/***
 * 这个getRandomList只能乱序连续整数list,例如1,2,3; 如果时1,3,5, 这个list返回的就不是这三个数字了
 * 而是1,2,3的组合
 * **/
    public static List<Integer> getRandomList(int num){
        //1.获取scope范围内的所有数值，并存到数组中
        int[] randomArray=new int[num];
        for(int i=0;i<randomArray.length;i++){
            randomArray[i]=i;
        }

        //2.从数组random中取数据，取过后的数改为-1
        int[] numArray=new int[num];//存储num个随机数
        int i=0;
        while(i<numArray.length){
            int index=(int)(Math.random()*num);
            if(randomArray[index]!=-1){
                numArray[i]=randomArray[index];
                randomArray[index]=-1;
                i++;
            }
        }
        ArrayList<Integer> arrayList = new ArrayList<>();
        for (int j=0;j<num;j++)
            arrayList.add(numArray[j]);
        return arrayList;
    }

    public static boolean checkEverSaveSongListIsEmpty(String string){
        return  Saver.readSongList(string)==null;
    }
    /**用于检查是否存在本地文件playlist**/
    public static List<Song> initSongList(String songListFileName){
        if (!checkEverSaveSongListIsEmpty(songListFileName))
            return   (List<Song>) Saver.readSongList(songListFileName);
        else
            return AudioUtils.getSongs(MyApplication.getContext());
    }
    /**用于检查是否存在本地playPic目录是否完全空的, 不是则返回false**/
    public static boolean checkEverInitPicCacheIsEmpty(){
        final String Local_Cache_Path =
                MyApplication.getContext().getExternalFilesDir(null).toString() + "cece" + "PicCache";
        File dir = new File(Local_Cache_Path);
        if (dir.exists()&&dir.listFiles().length>0)
            return false;
        else
            return true;
    }
    /**用来检查歌曲是否有专辑图片, 有则setAlbumPicExist=true, 无则setAlbumPicExist=false, 顺便把id也设成0
     * 但是这个方法不好, 所以不用, 而且后来检查发现这个方法逻辑写反了**/
    public static void initAlbumId(List<Song> songList){
        MediaMetadataRetriever mediaMetadataRetriever =new MediaMetadataRetriever();
        for (int i=0;i<songList.size();i++){
            mediaMetadataRetriever.setDataSource(songList.get(i).getPath());
            if (mediaMetadataRetriever.getEmbeddedPicture()==null){
                songList.get(i).setAlbum_Picture_Id(0);
                //这里写反了,估计是后来在改下面的方法看成这个方法误加的
                songList.get(i).setAlbumPicExist(true);
            }
        }
    }
    /**用来检查歌曲是否有专辑图片, 有则setAlbumPicExist=true, 无则setAlbumPicExist=false, 顺便把id也设成0
     * 这个是能用的**/
    public static void initAlbumId2(List<Song> songList){
        MediaMetadataRetriever mediaMetadataRetriever =new MediaMetadataRetriever();
        for (int i=0;i<songList.size();i++){
            mediaMetadataRetriever.setDataSource(songList.get(i).getPath());
            if (mediaMetadataRetriever.getEmbeddedPicture()!=null)
                songList.get(i).setAlbumPicExist(true);
            else{
                songList.get(i).setAlbumPicExist(false);
                songList.get(i).setAlbum_Picture_Id(0);
            }

        }
    }
/**这个方法根据id来判断专辑图片, 不够完美,弃用**/
    public static void initPicCache(List<Song> songList){
        Bitmap bitmap ;
        for (int i=0;i<songList.size();i++){
            if (songList.get(i).getAlbum_Picture_Id()!=0){
                bitmap =bitmapTo128N( loadingCover(songList.get(i).getPath()) );
                Saver.setLocalCachePath(" "+songList.get(i).getAlbum_Picture_Id(), bitmap, 100);
            }
        }
    }
    /**这个方法根据id和exist来判断专辑图片, 完美,启用**/
    public static void initPicCache2(List<Song> songList){
        Bitmap bitmap ;
        for (int i=0;i<songList.size();i++){
            if (songList.get(i).getIsAlbumPicExist()){
                bitmap =bitmapTo128N( loadingCover(songList.get(i).getPath()) );
                Saver.setLocalCachePath(" "+songList.get(i).getAlbum_Picture_Id(), bitmap, 100);
            }
        }
    }
    /**弃用了**/
    public static void  initAlbumPicCache(final List<Song> songList){
        final List<Song> songList_final =songList;
        if (checkEverInitPicCacheIsEmpty()){
            Thread thread=new Thread(new Runnable() {
                @Override
                public void run() {
                    initAlbumId(songList_final);
                    initPicCache(songList_final);
                }
            });
            thread.start();
        }
    }
/**自己写的压缩图片方法, 粗暴简单, 但是因为只有整数运算, 不可避免的把一部分内容去掉了, 不用了**/
    public static Bitmap bitmapTo128(Bitmap bitmap1){
        int width, height, ratio;
        ratio= bitmap1.getHeight()<bitmap1.getWidth()? bitmap1.getHeight()/128: bitmap1.getWidth()/128;
        if (ratio>6){
            Bitmap bitmap2=Bitmap.createBitmap(128,128,Bitmap.Config.ARGB_8888);
            bitmap2.setWidth(128);
            bitmap2.setHeight(128);
            for (int i=0;i<128; i=i+1) {
                for (int j = 0; j < 128; j=j+1) {
                    bitmap2.setPixel(i,j,  bitmap1.getPixel(i*ratio,j*ratio)  );
                }
            }
            return bitmap2;
        }
        return bitmap1;
    }
/**自带的压缩图片方法, 很强大**/
    public static Bitmap bitmapTo128N(Bitmap bitmap){
        int src_w = bitmap.getWidth();
        int src_h = bitmap.getHeight();
        float scale_w = ((float) 128) / src_w;
        float scale_h = ((float) 128) / src_h;
        Matrix matrix = new Matrix();
        matrix.postScale(scale_w, scale_h);
        Bitmap dstbmp = Bitmap.createBitmap(bitmap,0,0,src_w,src_h,matrix,true);
        return dstbmp;
    }

/**
 * 先把所有歌曲都检测一遍, 有专辑封面的设置成true, 没有的设置成false
 * 然后再根据true和false来判断需不需要缓存专辑封面
 * 然后根据是否存在AlbumID来把sameAlbumList中第一个albumIdExist为true的song的albumID提取出来
 * 然后跟他同一个album的但是没有专辑封面的song的albumID都设置成跟他一样的albumID
 * 注意, 没有封面的song其albumExist依旧是false
 * **/
    public static void cache(final List<Song> songList, final List<SameStringIdList> sameAlbumList){
        new Thread(new Runnable() {
            @Override
            public void run() {
                initAlbumId2(songList);
                initPicCache2(songList);

                for (int i = 0; i<sameAlbumList.size();i++){
                    for (int j = 0;j<sameAlbumList.get(i).getList().size();j++){
                        if (songList.get(  (int)sameAlbumList.get(i).getList().get(j)).getIsAlbumPicExist()  ){
                            for (int k=0;k<sameAlbumList.get(i).getList().size();k++){
                                if ( !songList.get((int)sameAlbumList.get(i).getList().get(k)).getIsAlbumPicExist() ){
                                    int albumId = songList.get(  (int)sameAlbumList.get(i).getList().get(j)).getAlbum_Picture_Id();
                                    songList.get(  (int)sameAlbumList.get(i).getList().get(k)    ).setAlbum_Picture_Id(albumId);
                                }
                            }
                            break;
                        }
                    }
                }
                Saver.saveSongList("firstList",songList);
            }
        }).start();
    }
}

```




