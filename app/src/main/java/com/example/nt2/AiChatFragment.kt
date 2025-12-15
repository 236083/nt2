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

// ★ 追加: 初期メッセージ表示のために必要
import android.os.Handler
import android.os.Looper
import android.widget.TextView // ChatAdapterで使用する可能性のため追加 (今回は仮定義内)

// =================================================================
// ★ 【仮定義】データモデルとアダプター (実運用では別ファイルに分離推奨)
// =================================================================

/**
 * チャットメッセージのデータクラス
 * @param id メッセージID (タイムスタンプなど)
 * @param message メッセージ本文
 * @param isUser true: ユーザー発言, false: AI発言
 */
data class ChatMessage(
    val id: Long,
    val message: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * チャットRecyclerView用のアダプター (簡易版)
 */
class ChatAdapter(private val messages: MutableList<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    // item_message.xml が必要です。ここではTextViewを仮定します。
    // item_message.xml には、ユーザー用とAI用のレイアウトを区別するビューが含まれる必要があります。
    private val VIEW_TYPE_USER = 1
    private val VIEW_TYPE_AI = 2

    // ★ onCreateViewHolder: 適切なレイアウトをインフレートする必要があります
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        // TODO: ここをプロジェクトのitem_message.xmlで置き換えてください
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    // ★ onBindViewHolder: データに応じてビューを設定する必要があります
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        // TODO: holder.textView.text = message.message のような処理
    }

    override fun getItemCount(): Int = messages.size

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isUser) VIEW_TYPE_USER else VIEW_TYPE_AI
    }

    // ★ 新規メッセージを追加するメソッド
    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // TODO: item_message.xml内のTextViewなどをここにバインドします
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

    // ★ 修正: ChatAdapterとメッセージリストを定義
    private lateinit var chatAdapter: ChatAdapter
    private val messageList = mutableListOf<ChatMessage>()


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

        // 2. RecyclerViewの初期設定
        setupRecyclerView()

        // 3. イベントリスナーの設定
        setupEventListeners()

        // ★ 追加: 初期メッセージの表示をトリガー
        displayInitialAiMessage()

        return rootView
    }

    // ... (onResume, onPause, onDestroyView は変更なし) ...

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
        // ★ 修正: アダプターの初期化
        chatAdapter = ChatAdapter(messageList)

        recyclerViewChat.layoutManager = LinearLayoutManager(context).apply {
            stackFromEnd = true // メッセージを下から上に積み上げる
        }
        recyclerViewChat.adapter = chatAdapter
    }

    private fun setupEventListeners() {
        buttonSend.setOnClickListener {
            sendMessage()
        }

        buttonChangePersona.setOnClickListener {
            // TODO: AIの性格変更ロジックを実装
        }

        buttonImageAnalysis.setOnClickListener {
            // TODO: 画像関連機能のロジックを実装
        }

        buttonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun sendMessage() {
        val messageText = editTextMessage.text.toString().trim()

        if (messageText.isNotEmpty()) {
            // 1. ユーザーメッセージを作成
            val userMessage = ChatMessage(
                id = System.currentTimeMillis(),
                message = messageText,
                isUser = true
            )

            // 2. アダプターにメッセージを追加し、リストを更新
            chatAdapter.addMessage(userMessage)
            recyclerViewChat.scrollToPosition(chatAdapter.itemCount - 1)

            // 3. 入力欄をクリア
            editTextMessage.setText("")

            // TODO: 4. AIからの疑似応答をトリガー (例: callAiResponse(messageText))
        }
    }

    /**
     * フラグメントが開かれたときにAIからの初期メッセージを表示する
     */
    private fun displayInitialAiMessage() {
        // AIからのウェルカムメッセージを定義
        val welcomeMessage = "こんにちは！私はあなたの専属ファッションAIアドバイザーです。\n" +
                "今日のコーデの相談や、持っている服を使った新しいスタイリングの提案、" +
                "特定のアイテム（例：Tシャツ）のトレンド調査など、何でもお尋ねください！"

        // AIメッセージオブジェクトを作成
        val initialAiMessage = ChatMessage(
            id = System.currentTimeMillis(),
            message = welcomeMessage,
            isUser = false // AIからのメッセージ
        )

        // 0.5秒の遅延を設けてメッセージをリストに追加し、自然な表示を演出
        Handler(Looper.getMainLooper()).postDelayed({
            // アダプターが初期化されていることを確認してから追加
            if (::chatAdapter.isInitialized) {
                chatAdapter.addMessage(initialAiMessage)
                recyclerViewChat.scrollToPosition(chatAdapter.itemCount - 1)
            }
        }, 500) // 0.5秒後に実行
    }


    companion object {
        @JvmStatic
        fun newInstance() = AiChatFragment()
    }
}