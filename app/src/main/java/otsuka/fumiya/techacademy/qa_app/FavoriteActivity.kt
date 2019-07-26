package otsuka.fumiya.techacademy.qa_app

import android.content.Intent
import android.graphics.Color
import android.location.SettingInjectorService
import android.os.Bundle
import android.util.Base64
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_question_detail.*
import kotlinx.android.synthetic.main.activity_question_send.*
import java.lang.ref.Reference

class FavoriteActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener{

    private var mQuestion = ArrayList<Question>()
    private var mQuestion1 = ArrayList<Question>()
    private var mQuestion2 = ArrayList<Question>()
    private var mQuestion3 = ArrayList<Question>()
    private var mQuestion4 = ArrayList<Question>()
    private var mFavolite = ArrayList<Favolite>()
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mListView: ListView
    private lateinit var mAdapter: favoListAdapter
    private lateinit var mToolbar: androidx.appcompat.widget.Toolbar
    private var mGenre = 0
    private lateinit var mFavoriteRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite)
        mToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(mToolbar)

        val extras = intent.extras
        mQuestion = extras!!.get("que") as ArrayList<Question>

//        for (i in 0..mQuestion.size){
//            when(mQuestion[i].genre){
//                1 -> mQuestion1.add(mQuestion[i])
//                2 -> mQuestion2.add(mQuestion[i])
//                3 -> mQuestion3.add(mQuestion[i])
//                4 -> mQuestion4.add(mQuestion[i])
//            }
//
//        }


        // ナビゲーションドロワーの設定
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(this, drawer, mToolbar, R.string.app_name, R.string.app_name)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)



        val dataBaseReference = FirebaseDatabase.getInstance().reference
        // ログイン済みのユーザーを取得する
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {

            mFavoriteRef =
                dataBaseReference.child(UsersPATH).child(user!!.uid).child(FavoritesPATH)

            mFavoriteRef.addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    val fData = snapshot.value as Map<String, String>?
                    var genre :String
                    if (fData != null){

                        for (key in fData.keys){
                            genre = fData["genre"]?: ""
                            Log.d("test_genre",genre)

                            for (question in mQuestion){
                                if (key == question.questionUid){

                                    var title = question.title
                                    var body = question.body
                                    var name = question.name
                                    var uid = question.uid
                                    var questionuid = question.questionUid
                                    var genre = question.genre
                                    var bytes = question.imageBytes
                                    var ansewers = question.answers

                                    var favorite = Favolite(title,body, name, uid, questionuid, genre, bytes, ansewers)

                                    mFavolite.add(favorite)

                                }
                            }
                        }
                    }
                }
                override fun onCancelled(firebaseError: DatabaseError) {}
            })

        }

        // ListViewの準備
        mListView = findViewById(R.id.favoListView)
        mAdapter = favoListAdapter(this)
        mAdapter.notifyDataSetChanged()

        mListView.setOnItemClickListener { _, _, position, _ ->
            // Questionのインスタンスを渡して質問詳細画面を起動する
            val intent = Intent(applicationContext, QuestionDetailActivity::class.java)
            intent.putExtra("question", mFavolite[position])
            startActivity(intent)
        }




    }

    override fun onNavigationItemSelected(item: MenuItem):Boolean {
        val id = item.itemId

        if (id == R.id.nav_hobby) {
            mToolbar.title = "趣味"
            mGenre = 1
        } else if (id == R.id.nav_life) {
            mToolbar.title = "生活"
            mGenre = 2
        } else if (id == R.id.nav_health) {
            mToolbar.title = "健康"
            mGenre = 3
        } else if (id == R.id.nav_compter) {
            mToolbar.title = "コンピューター"
            mGenre = 4
        } else if (id == R.id.nav_Main){
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            return true
        }
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)

//        // 質問のリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
//        mQuestionArrayList.clear()
//        mAdapter.setQuestionArrayList(mQuestionArrayList)
//        mListView.adapter = mAdapter
//
//        // 選択したジャンルにリスナーを登録する
//        if (mGenreRef != null) {
//            mGenreRef!!.removeEventListener(mEventListener)
//        }
//        mGenreRef = mDatabaseReference.child(ContentsPATH).child(mGenre.toString())
//        mGenreRef!!.addChildEventListener(mEventListener)

        return true
    }



}
