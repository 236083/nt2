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

// ★ 追加: ダイアログとトースト用
import android.app.AlertDialog
import android.widget.Toast

import android.os.Handler
import android.os.Looper

// =================================================================
// ★ 【仮定義】データモデルとアダプター (変更なし)
// =================================================================
data class ChatMessage(
    val id: Long,
    val message: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class ChatAdapter(private val messages: MutableList<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    private val VIEW_TYPE_USER = 1
    private val VIEW_TYPE_AI = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        // ここをプロジェクトのitem_message.xmlで置き換えてください
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        // holder.textView.text = messages[position].message のような処理
    }

    override fun getItemCount(): Int = messages.size

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isUser) VIEW_TYPE_USER else VIEW_TYPE_AI
    }

    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    // ★ 修正: 全メッセージをクリアするメソッドを追加
    fun clearAllMessages() {
        messages.clear()
        notifyDataSetChanged()
    }

    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        //item_message.xml内のTextViewなどをここにバインドします
        // val textView: TextView = view.findViewById(R.id.text_message_content)
    }
}
// =================================================================

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
    private lateinit var buttonBack: ImageButton

    // ★ 新規追加: チャット履歴/テンプレートボタン
    private lateinit var buttonClearChat: ImageButton
    private lateinit var buttonTemplates: ImageButton

    // ★ 修正: ChatAdapterとメッセージリストを定義
    private lateinit var chatAdapter: ChatAdapter
    private val messageList = mutableListOf<ChatMessage>()

    // ★ 新規追加: 現在のAIの性格を保持する変数
    private var currentPersona: String = "優しめ" // 初期値

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_ai_chat, container, false)

        // 1. ビューのバインド
        recyclerViewChat = rootView.findViewById(R.id.recycler_view_chat)
        editTextMessage = rootView.findViewById(R.id.edit_text_message)
        buttonSend = rootView.findViewById(R.id.button_send)
        buttonChangePersona = rootView.findViewById(R.id.button_change_persona)
        buttonImageAnalysis = rootView.findViewById(R.id.button_image_analysis_generation)
        buttonBack = rootView.findViewById(R.id.button_back)

        // ★ 新規追加: ボタンのバインド
        buttonClearChat = rootView.findViewById(R.id.button_clear_chat)
        buttonTemplates = rootView.findViewById(R.id.button_templates)

        // 2. RecyclerViewの初期設定
        setupRecyclerView()

        // 3. イベントリスナーの設定
        setupEventListeners()

        // ★ 初期メッセージの表示をトリガー
        displayInitialAiMessage()

        return rootView
    }

    // ... (setBottomNavVisibility, onResume, onPause, onDestroyView は省略) ...

    private fun setBottomNavVisibility(isVisible: Boolean) {
        val activity = activity ?: return
        val bottomNavView: BottomNavigationView = activity.findViewById(R.id.bottom_navigation)
        bottomNavView.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        setBottomNavVisibility(false)
    }

    override fun onPause() {
        super.onPause()
        setBottomNavVisibility(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        setBottomNavVisibility(true)
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(messageList)

        recyclerViewChat.layoutManager = LinearLayoutManager(context).apply {
            stackFromEnd = true
        }
        recyclerViewChat.adapter = chatAdapter
    }

    private fun setupEventListeners() {
        buttonSend.setOnClickListener {
            sendMessage()
        }

        // ★ 修正: AI性格変更ダイアログ
        buttonChangePersona.setOnClickListener {
            showPersonaChangeDialog()
        }

        buttonImageAnalysis.setOnClickListener {
            // 画像関連機能のロジックを実装
            showToast("画像分析/生成機能は未実装です。")
        }

        buttonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // ★ 新規追加: チャット履歴クリア
        buttonClearChat.setOnClickListener {
            showClearChatConfirmationDialog()
        }

        // ★ 新規追加: テンプレート質問
        buttonTemplates.setOnClickListener {
            showTemplateQuestionsDialog()
        }
    }

    private fun sendMessage() {
        val messageText = editTextMessage.text.toString().trim()

        if (messageText.isNotEmpty()) {
            val userMessage = ChatMessage(
                id = System.currentTimeMillis(),
                message = messageText,
                isUser = true
            )
            chatAdapter.addMessage(userMessage)
            recyclerViewChat.scrollToPosition(chatAdapter.itemCount - 1)
            editTextMessage.setText("")
            //  4. AIからの疑似応答をトリガー (例: callAiResponse(messageText))
        }
    }

    /**
     * フラグメントが開かれたときにAIからの初期メッセージを表示する
     */
    private fun displayInitialAiMessage() {
        val welcomeMessage = "こんにちは！私はあなたの専属ファッションAIアドバイザーです。\n" +
                "今日のコーデの相談や、持っている服を使った新しいスタイリングの提案、" +
                "特定のアイテム（例：Tシャツ）のトレンド調査など、何でもお尋ねください！"

        val initialAiMessage = ChatMessage(
            id = System.currentTimeMillis(),
            message = welcomeMessage,
            isUser = false
        )

        Handler(Looper.getMainLooper()).postDelayed({
            if (::chatAdapter.isInitialized) {
                chatAdapter.addMessage(initialAiMessage)
                recyclerViewChat.scrollToPosition(chatAdapter.itemCount - 1)
            }
        }, 500)
    }

    // =================================================================
    // ★ 新規追加: ダイアログと機能のロジック
    // =================================================================

    /**
     * AIの性格（ペルソナ）を変更するための選択ダイアログを表示する。
     */
    private fun showPersonaChangeDialog() {
        val personas = arrayOf("優しめ", "辛口", "毒舌")
        val checkedItem = personas.indexOf(currentPersona)

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("AIの話し方を変える")
            .setSingleChoiceItems(personas, checkedItem) { dialog, which ->
                currentPersona = personas[which]
            }
            .setPositiveButton("決定") { dialog, id ->
                showToast("AIの話し方を「$currentPersona」に変更しました。")
                //  AIのバックエンド設定を新しいペルソナで更新するロジックを実装
            }
            .setNegativeButton("キャンセル") { dialog, id ->
                dialog.cancel()
            }

        builder.create().show()
    }

    /**
     * チャット履歴削除の確認ダイアログを表示する。
     */
    private fun showClearChatConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("チャット履歴の削除")
            .setMessage("チャット履歴をすべて削除しますか？\n(AIの記憶はリセットされません)")
            .setPositiveButton("削除") { dialog, id ->
                if (::chatAdapter.isInitialized) {
                    chatAdapter.clearAllMessages()
                    showToast("チャット履歴を削除しました。")
                    // 削除後、AIの初期メッセージを再表示する
                    displayInitialAiMessage()
                }
            }
            .setNegativeButton("キャンセル") { dialog, id ->
                dialog.cancel()
            }
        builder.create().show()
    }

    /**
     * テンプレート質問リストを表示し、選択した質問をEditTextに設定する。
     */
    private fun showTemplateQuestionsDialog() {
        val templates = arrayOf(
            "この白いTシャツに合うアウターを3つ提案して。",
            "今日のトレンドカラーは何ですか？",
            "持っているデニムジャケットを使った着回しコーデを見せて。",
            "「優しめ」モードでの今日の運勢を教えて。",
            "オフィスカジュアルに合う、春の新作バッグを探して。"
        )

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("質問テンプレート")
            .setItems(templates) { dialog, which ->
                val selectedTemplate = templates[which]
                editTextMessage.setText(selectedTemplate)
                showToast("質問をメッセージ欄に設定しました。")
            }
            .setNegativeButton("閉じる") { dialog, id ->
                dialog.cancel()
            }
        builder.create().show()
    }

    /**
     * トーストメッセージを表示するヘルパー関数
     */
    private fun showToast(message: String) {
        context?.let {
            Toast.makeText(it, message, Toast.LENGTH_SHORT).show()
        }
    }


    companion object {
        @JvmStatic
        fun newInstance() = AiChatFragment()
    }
}