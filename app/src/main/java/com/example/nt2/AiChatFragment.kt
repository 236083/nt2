package com.example.nt2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * AIチャット画面のFragmentクラス。
 */
class AiChatFragment : Fragment() {

    // ビューの定義
    private lateinit var recyclerViewChat: RecyclerView
    private lateinit var editTextMessage: EditText
    private lateinit var buttonSend: ImageButton
    private lateinit var buttonChangePersona: MaterialButton
    private lateinit var buttonImageAnalysis: MaterialButton
    private lateinit var buttonBack: ImageButton // ★ 追加: 戻るボタン

    // TODO: ここに ChatAdapter を定義・初期化する必要があります
    // private lateinit var chatAdapter: ChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // レイアウトをインフレートし、rootViewを取得
        val rootView = inflater.inflate(R.layout.fragment_ai_chat, container, false)

        // 1. ビューのバインド
        recyclerViewChat = rootView.findViewById(R.id.recycler_view_chat)
        editTextMessage = rootView.findViewById(R.id.edit_text_message)
        buttonSend = rootView.findViewById(R.id.button_send)
        buttonChangePersona = rootView.findViewById(R.id.button_change_persona)
        buttonImageAnalysis = rootView.findViewById(R.id.button_image_analysis_generation)
        buttonBack = rootView.findViewById(R.id.button_back) // ★ 追加: 戻るボタンをバインド

        // 2. RecyclerViewの初期設定
        setupRecyclerView()

        // 3. イベントリスナーの設定
        setupEventListeners()

        return rootView
    }

    private fun setBottomNavVisibility(isVisible: Boolean) {
        val activity = activity ?: return
        val bottomNavView: BottomNavigationView = activity.findViewById(R.id.bottom_navigation)
        bottomNavView.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        // AIチャット画面が表示されたとき、BottomNavigationViewを非表示にする
        setBottomNavVisibility(false)
    }

    override fun onPause() {
        super.onPause()
        // AIチャット画面が非表示になったとき、BottomNavigationViewを再表示する
        // ※ 戻るボタンで画面を閉じる際も、この処理が実行されます
        setBottomNavVisibility(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        setBottomNavVisibility(true)
    }

    private fun setupRecyclerView() {
        // ... 既存の実装 ...
        recyclerViewChat.layoutManager = LinearLayoutManager(context).apply {
            stackFromEnd = true
        }
    }

    private fun setupEventListeners() {
        // 送信ボタンのクリックリスナー
        buttonSend.setOnClickListener {
            sendMessage()
        }

        // AI変更ボタンのクリックリスナー
        buttonChangePersona.setOnClickListener {
            // TODO: AIの性格変更ロジックを実装
        }

        // 画像分析/生成ボタンのクリックリスナー
        buttonImageAnalysis.setOnClickListener {
            // TODO: 画像関連機能のロジックを実装
        }

        // ★ 追加: 戻るボタンのクリックリスナー
        buttonBack.setOnClickListener {
            // Fragmentをスタックからポップし、前の画面に戻る
            parentFragmentManager.popBackStack()
        }
    }

    private fun sendMessage() {
        val messageText = editTextMessage.text.toString().trim()

        if (messageText.isNotEmpty()) {
            // TODO: 1. メッセージオブジェクトを作成 (User)
            // TODO: 2. アダプターにメッセージを追加し、リストを更新
            // TODO: 3. 入力欄をクリア
            // TODO: 4. AIからの疑似応答をトリガー

            editTextMessage.setText("") // 入力欄をクリア
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = AiChatFragment()
    }
}