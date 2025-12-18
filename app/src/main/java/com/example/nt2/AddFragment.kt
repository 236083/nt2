package com.example.nt2

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.*

class AddFragment : Fragment() {

    data class Schedule(var title: String, var content: String)
    private val scheduleMap = mutableMapOf<String, Schedule>()
    private var selectedDate: String = ""

    // 各Viewの宣言
    private lateinit var btnAdd: View
    private lateinit var layoutButtons: View
    private lateinit var layoutDetail: View
    private lateinit var textTitle: TextView
    private lateinit var textContent: TextView
    private lateinit var textSelectedDate: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_add, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Viewの初期化（IDがレイアウトと一致しているか確認してください）
        val calendarView = view.findViewById<CalendarView>(R.id.calendarView)
        textSelectedDate = view.findViewById(R.id.textSelectedDate)
        btnAdd = view.findViewById(R.id.btn_add_schedule)
        layoutButtons = view.findViewById(R.id.layout_edit_delete_buttons) // ここが重要
        layoutDetail = view.findViewById(R.id.layout_schedule_detail)
        textTitle = view.findViewById(R.id.text_schedule_title)
        textContent = view.findViewById(R.id.text_schedule_content)
        val btnEdit = view.findViewById<Button>(R.id.btn_edit_schedule)
        val btnDelete = view.findViewById<ImageButton>(R.id.btn_delete_schedule)

        // 初期表示設定
        val sdf = SimpleDateFormat("yyyy年MM月dd日", Locale.JAPAN)
        selectedDate = sdf.format(Date(calendarView.date))
        textSelectedDate.text = selectedDate
        updateUI()

        // 日付選択イベント
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = "${year}年${month + 1}月${dayOfMonth}日"
            textSelectedDate.text = selectedDate
            updateUI()
        }

        // 新規作成
        btnAdd.setOnClickListener {
            showEditDialog(null) { updateUI() }
        }

        // 編集
        btnEdit.setOnClickListener {
            showEditDialog(scheduleMap[selectedDate]) { updateUI() }
        }

        // 削除
        btnDelete.setOnClickListener {
            scheduleMap.remove(selectedDate)
            updateUI()
            Toast.makeText(requireContext(), "削除しました", Toast.LENGTH_SHORT).show()
        }
    }

    // UIの表示切り替えロジック
    private fun updateUI() {
        val schedule = scheduleMap[selectedDate]
        if (schedule == null) {
            // 予定がない場合：追加ボタンを表示、編集削除ボタンと詳細を隠す
            btnAdd.visibility = View.VISIBLE
            layoutButtons.visibility = View.GONE
            layoutDetail.visibility = View.GONE
        } else {
            // 予定がある場合：追加ボタンを隠し、編集削除ボタンと詳細を表示
            btnAdd.visibility = View.GONE
            layoutButtons.visibility = View.VISIBLE
            layoutDetail.visibility = View.VISIBLE
            textTitle.text = schedule.title
            textContent.text = schedule.content
        }
    }

    private fun showEditDialog(existing: Schedule?, onSave: () -> Unit) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_schedule, null)
        val editTitle = dialogView.findViewById<EditText>(R.id.edit_dialog_title)
        val editContent = dialogView.findViewById<EditText>(R.id.edit_dialog_content)
        val titleDisplay = dialogView.findViewById<TextView>(R.id.dialog_title_text)

        val btnCancel = dialogView.findViewById<View>(R.id.btn_dialog_cancel)
        val btnSave = dialogView.findViewById<View>(R.id.btn_dialog_save)

        if (existing != null) {
            titleDisplay.text = "予定を編集"
            editTitle.setText(existing.title)
            editContent.setText(existing.content)
        } else {
            titleDisplay.text = "新規作成 (${selectedDate})"
        }

        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnSave.setOnClickListener {
            val t = editTitle.text.toString()
            val c = editContent.text.toString()
            if (t.isNotBlank()) {
                scheduleMap[selectedDate] = Schedule(t, c)
                onSave()
                dialog.dismiss()
            } else {
                editTitle.error = "タイトルを入力してください"
            }
        }
    }
}