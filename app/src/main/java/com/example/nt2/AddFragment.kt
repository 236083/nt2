package com.example.nt2

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.nt2.databinding.FragmentAddBinding
import java.text.SimpleDateFormat
import java.util.*

/**
 * カレンダー画面Fragmentです。
 * カレンダー表示、日付選択、および新規作成ボタンによる入力欄の表示を担当します。
 */
class AddFragment : Fragment() {

    private var _binding: FragmentAddBinding? = null
    // View Binding のゲッター
    private val binding get() = _binding!!

    // 現在選択されている日付を保持 (保存ログ用)
    private var selectedDateString: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // View Binding を初期化
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 初期表示: 今日の日付を表示
        val todayFormatter = SimpleDateFormat("yyyy年M月d日", Locale.JAPAN)
        selectedDateString = todayFormatter.format(Date())
        binding.textSelectedDate.text = selectedDateString

        // 初期状態では入力コンテナを非表示にしておく
        binding.inputContainerCard.visibility = View.GONE



        // CalendarView Listener: 日付を選択した際の処理
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            // 選択された日付をCalendarオブジェクトで取得
            val calendar = Calendar.getInstance().apply {
                // CalendarViewのmonthは0-indexed (0=1月, 11=12月)
                set(year, month, dayOfMonth)
            }

            // 日付を「yyyy年M月d日」形式にフォーマット
            val dateFormat = SimpleDateFormat("yyyy年M月d日", Locale.JAPAN)
            selectedDateString = dateFormat.format(calendar.time)

            // 1. テキストビューに表示を更新
            binding.textSelectedDate.text = selectedDateString

            // 【バグ修正ロジック】: 新規作成モード中に別の日付を選択した場合、入力欄を非表示に戻す
            if (binding.inputContainerCard.visibility == View.VISIBLE) {
                // 入力欄を非表示に戻す
                binding.inputContainerCard.visibility = View.GONE
                Toast.makeText(context, "新規作成モードをリセットしました", Toast.LENGTH_SHORT).show()
            }
        }

        // 新規メモ作成ボタンのクリックリスナー
        binding.buttonNewMemo.setOnClickListener {
            // 入力コンテナを表示する
            binding.inputContainerCard.visibility = View.VISIBLE

            // 入力欄をクリアする (新しいメモ作成のため)
            binding.editTitle.setText("")
            binding.editContent.setText("")

            // (オプション) 入力欄までスムーズにスクロール
            (binding.root.parent as? ScrollView)?.post {
                binding.inputContainerCard.requestFocus()
            }
        }

        // 保存ボタンのクリックリスナー
        binding.buttonSave.setOnClickListener {
            val title = binding.editTitle.text.toString().trim()
            val content = binding.editContent.text.toString().trim()

            if (title.isNotEmpty() || content.isNotEmpty()) {
                // 実際にはここでデータを保存します
                Log.d("AddFragment", "保存されました - 日付: $selectedDateString, タイトル: $title, 内容: $content")
                Toast.makeText(context, "「$title」を保存しました", Toast.LENGTH_SHORT).show()

                // 保存後、入力欄を非表示にする
                binding.inputContainerCard.visibility = View.GONE

            } else {
                Toast.makeText(context, "タイトルか内容を入力してください", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}