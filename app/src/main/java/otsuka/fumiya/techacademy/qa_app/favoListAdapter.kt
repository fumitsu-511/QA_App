package otsuka.fumiya.techacademy.qa_app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class favoListAdapter (context: Context): BaseAdapter() {
        private var mLayoutInflater: LayoutInflater
        private var FavoliteArraylist = ArrayList<Question>()

        init {
            mLayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        }

        override fun getCount(): Int {
            return FavoliteArraylist.size
        }

        override fun getItem(position: Int): Any {
            return FavoliteArraylist[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView

            if (convertView == null){
                convertView = mLayoutInflater.inflate(R.layout.list_questions, parent, false)
            }

            val titleText = convertView!!.findViewById<View>(R.id.titleTextView) as TextView
            titleText.text = FavoliteArraylist[position].title

            val nameText = convertView.findViewById<View>(R.id.nameTextView) as TextView
            nameText.text = FavoliteArraylist[position].name

            val resText = convertView.findViewById<View>(R.id.resTextView) as TextView
            val resNum = FavoliteArraylist[position].answers.size
            resText.text = resNum.toString()

            val bytes = FavoliteArraylist[position].imageBytes
            if (bytes.isNotEmpty()) {
                val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size).copy(Bitmap.Config.ARGB_8888, true)
                val imageView = convertView.findViewById<View>(R.id.imageView) as ImageView
                imageView.setImageBitmap(image)
            }

            return convertView!!
        }

        fun setFavoriteArrayList(favoliteArrayList: ArrayList<Question>){
            FavoliteArraylist = favoliteArrayList
        }
    }