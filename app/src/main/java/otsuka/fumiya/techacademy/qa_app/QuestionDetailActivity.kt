package otsuka.fumiya.techacademy.qa_app

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

import kotlinx.android.synthetic.main.activity_question_detail.*
import kotlinx.android.synthetic.main.list_question_detail.*


class QuestionDetailActivity : AppCompatActivity() {

    private lateinit var mQuestion: Question
    private lateinit var mAdapter: QuestionDetailListAdapter
    private lateinit var mAnswerRef: DatabaseReference
    private lateinit var mFavoriteRef: DatabaseReference
    private var favoJudge = false

    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>

            val answerUid = dataSnapshot.key ?: ""

            for (answer in mQuestion.answers) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid == answer.answerUid) {
                    return
                }
            }

            val body = map["body"] ?: ""
            val name = map["name"] ?: ""
            val uid = map["uid"] ?: ""

            val answer = Answer(body, name, uid, answerUid)
            mQuestion.answers.add(answer)
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {

        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_detail)

        // 渡ってきたQuestionのオブジェクトを保持する
        val extras = intent.extras
        mQuestion = extras!!.get("question") as Question

        title = mQuestion.title

        // ListViewの準備
        mAdapter = QuestionDetailListAdapter(this, mQuestion)
        listView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

        fab.setOnClickListener {
            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) {
                // ログインしていなければログイン画面に遷移させる
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                // Questionを渡して回答作成画面を起動する
                // --- ここから ---
                val intent = Intent(applicationContext, AnswerSendActivity::class.java)
                intent.putExtra("question", mQuestion)
                startActivity(intent)
                // --- ここまで ---
            }
        }

        val dataBaseReference = FirebaseDatabase.getInstance().reference
        mAnswerRef =
            dataBaseReference.child(ContentsPATH).child(mQuestion.genre.toString()).child(mQuestion.questionUid)
                .child(AnswersPATH)
        mAnswerRef.addChildEventListener(mEventListener)

        //お気に入りのコード----------------------------------------------------------
        // ログイン済みのユーザーを取得する
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {

            mFavoriteRef =
                dataBaseReference.child(UsersPATH).child(user!!.uid).child(FavoritesPATH)

            mFavoriteRef.addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    val fData = snapshot.value as Map<String, String>?
                    if (fData != null){
                        for (key in fData.keys){
                            Log.d("keytest",key)
                            if (key == mQuestion.questionUid){
                                favoJudge = true
                                favoriteButton.setBackgroundColor(Color.YELLOW)
                                break
                            }
                        }
                    }
                }
                override fun onCancelled(firebaseError: DatabaseError) {}
            })

        } else{
            favoriteButton.visibility= View.GONE
        }

        favoriteButton.setOnClickListener {

            mFavoriteRef =
                dataBaseReference.child(UsersPATH).child(user!!.uid).child(FavoritesPATH).child(mQuestion.questionUid)

            if(favoJudge==true){
                mFavoriteRef.removeValue()
                favoriteButton.setBackgroundColor(Color.WHITE)
                favoJudge = false

            } else if(favoJudge==false){
                val fData = HashMap<String, String>()
                fData["uid"] = mQuestion.questionUid
                fData["genre"] = mQuestion.genre.toString()
                mFavoriteRef.setValue(fData)

                favoriteButton.setBackgroundColor(Color.YELLOW)
                favoJudge = true
            }
        }
        //-----------------------------------------------------------------------------

    }
}