package jp.techacademy.minami.jommo.autoslideshowapp

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.provider.MediaStore
import android.content.ContentUris
import android.net.Uri
import android.os.Handler
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 100

    var imgUriList:MutableList<Uri> = mutableListOf()
    var listNum:Int = 1
    var listLen:Int = 1

    private var mTimer: Timer? = null
    private var mHandler = Handler()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo()
        }


        back_button.setOnClickListener {
            listNum--
            setImage(listNum)
        }
                forward_button.setOnClickListener{
            listNum++
            setImage(listNum)
        }

        start_button.setOnClickListener{
            if (mTimer == null){
                hyoji()
                mTimer = Timer()
                mTimer!!.schedule(object : TimerTask() {
                    override fun run() {
                        mHandler.post {
                            listNum++
                            setImage(listNum)
                        }
                    }
                }, 2000, 2000)
            }else{
                hyoji()
                mTimer!!.cancel()
                mTimer = null
            }
        }
    }

    private fun hyoji(){
        if(mTimer == null){
            start_button.text="停止"
            back_button.text = ""
            forward_button.text=""
        }else{
            start_button.text="再生"
            back_button.text = "戻る"
            forward_button.text="進む"
        }
    }

    private fun setImage(ln:Int){

        if(listNum==listLen){
            listNum = 1
        }else if(ln==0){
            listNum = listLen
        }

        if(0<listNum && listNum<listLen)
        imageView.setImageURI(imgUriList[listNum])
        textView.text = "${imgUriList[listNum]}"
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }
        }
    }


    private fun getContentsInfo() {
        // 画像の情報を取得する
        val resolver = contentResolver
        val cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
        )


        if (cursor.moveToFirst()) {

            do {
                // indexからIDを取得し、そのIDから画像のURIを取得する
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(fieldIndex)
                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                imgUriList.add(imageUri)
            } while (cursor.moveToNext())

            listLen = imgUriList.count()
            imgUriList.add(0,imgUriList[0])     // とにかくゼロは使いたくない
            for((i,a) in imgUriList.withIndex()){Log.d("ANDROID",  "imgUriList[${i}] = ${a}")}
        }
        setImage(listNum)
        cursor.close()

    }
}