package otsuka.fumiya.techacademy.qa_app

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem
import android.widget.ListView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
class FavoriteActivity : AppCompatActivity(){

    private var mFavolite = ArrayList<Question>()
    private lateinit var mListView: ListView
    private lateinit var mAdapter: favoListAdapter
    private lateinit var mFavoriteRef: DatabaseReference
    private var Uid :String? = null
    private lateinit var Genre:String
    private var mUid=ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite)

        title = "お気に入り一覧"

        val dataBaseReference = FirebaseDatabase.getInstance().reference
        // ログイン済みのユーザーを取得する
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {

            mFavoriteRef =
                dataBaseReference.child(UsersPATH).child(user!!.uid).child(FavoritesPATH)

            mFavoriteRef.addChildEventListener(mEventListener)


        }

        // ListViewの準備
        mListView = findViewById(R.id.favoListView)
        mAdapter = favoListAdapter(this)
        mAdapter.setFavoriteArrayList(mFavolite)
        mListView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

        mListView.setOnItemClickListener { _, _, position, _ ->
            // Questionのインスタンスを渡して質問詳細画面を起動する
            val intent = Intent(applicationContext, QuestionDetailActivity::class.java)
            intent.putExtra("question", mFavolite[position])
            startActivity(intent)
        }
    }


    private val mEventListener = object :ChildEventListener{
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {

            val fData = dataSnapshot.value as Map<String, String>?
            Uid = dataSnapshot.key
            mUid.add(Uid!!)

            if (fData != null){

                for (key in fData.keys){

                    if (key=="genre"){
                        Genre =fData[key]!!
                    }

                }

                val dataBaseReference = FirebaseDatabase.getInstance().reference
                val genreRef = dataBaseReference.child(ContentsPATH).child(Genre)
                genreRef.addChildEventListener(fEventListener)
            }
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
        override fun onChildRemoved(p0: DataSnapshot) {}
        override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
        override fun onCancelled(p0: DatabaseError) {}

    }

    private val fEventListener  = object :ChildEventListener{
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {

            val key = dataSnapshot.key
            var fData = dataSnapshot.value as Map<String,String>

            for (i in 0..mUid.size-1){
                var tempUID = mUid[i]
                if (key ==tempUID){
                    var title = fData["title"] ?:""
                    var body = fData["body"] ?:""
                    var name = fData["name"] ?:""
                    var tempuid =  fData["uid"] ?:""
                    var questionUid = Uid!!
                    val imageString = fData["image"] ?: ""
                    val bytes =
                        if (imageString.isNotEmpty()){
                            Base64.decode(imageString, Base64.DEFAULT)
                        } else{
                            byteArrayOf()
                        }

                    val answerArrayList = ArrayList<Answer>()
                    val answerMap = fData["answers"] as Map<String, String>?
                    if (answerMap != null){
                        for (ansKey in answerMap.keys){
                            val temp = answerMap[ansKey] as Map<String, String>
                            val answerBody = temp["body"] ?: ""
                            val answerName = temp["name"] ?: ""
                            val answerUid = temp["uid"] ?: ""
                            val answer = Answer(answerBody, answerName, answerUid, ansKey)
                            answerArrayList.add(answer)
                        }
                    }

                    var favorite = Question(title,body,name,tempuid,questionUid.toString(),Genre.toInt(),bytes,answerArrayList)
                    mFavolite.add(favorite)

                    mAdapter.notifyDataSetChanged()

                    mUid[i] = ""
                }

            }


        }
        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>

            //変更があったQuestionを探す
            for (question in mFavolite){
                if (dataSnapshot.key.equals(question.questionUid)){
                    //このアプリで変更のある可能性があるのは回答(Answer)のみ
                    question.answers.clear()
                    val answerMap = map["answers"] as Map<String, String>?
                    if (answerMap != null){
                        for(ansKey in answerMap.keys){
                            val temp = answerMap[ansKey] as Map<String, String>
                            val answerBody = temp["body"] ?: ""
                            val answerName = temp["name"] ?: ""
                            val answerUid = temp["uid"] ?: ""
                            val answer = Answer(answerBody, answerName, answerUid, ansKey)
                            question.answers.add(answer)
                        }
                    }

                    mAdapter.notifyDataSetChanged()
                }
            }
        }

        override fun onChildRemoved(p0: DataSnapshot) {}

        override fun onChildMoved(p0: DataSnapshot, p1: String?) {}

        override fun onCancelled(p0: DatabaseError) {}
    }



}
