package com.example.nt2

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import java.util.*

class ProfiSetting : Fragment() {

    private lateinit var textBirthdateValue: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profi_setting, container, false)

        textBirthdateValue = view.findViewById(R.id.text_birthdate_value)

        // ヘッダーの戻るボタンの処理
        val backButton: ImageButton = view.findViewById(R.id.btn_back)
        backButton.setOnClickListener {
            // Fragmentをポップして前の画面に戻る
            parentFragmentManager.popBackStack()
        }

        // 生年月日 (TextView) のクリック処理
        textBirthdateValue.setOnClickListener {
            showDatePicker()
        }

        // 保存ボタンの処理
        val saveButton: Button = view.findViewById(R.id.btn_save_profile)
        saveButton.setOnClickListener {
            // ここにフォーム入力値の取得と保存処理を記述します
            Toast.makeText(requireContext(), "プロフィールを保存中です...", Toast.LENGTH_SHORT).show()

            // 保存成功後、前の画面に戻ることをシミュレーション
            parentFragmentManager.popBackStack()
        }

        return view
    }

    /**
     * DatePickerDialog (日付選択ダイアログ) を表示する関数
     */
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                // ユーザーが日付を選択した時の処理
                // 月は 0 から始まるため +1 する
                val dateString = String.format(
                    Locale.JAPAN,
                    "%d年%d月%d日",
                    selectedYear,
                    selectedMonth + 1,
                    selectedDay
                )
                textBirthdateValue.text = dateString
                textBirthdateValue.setTextColor(resources.getColor(android.R.color.black, null)) // テキスト色を黒に変更
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }
}