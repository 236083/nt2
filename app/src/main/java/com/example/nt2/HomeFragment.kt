package com.example.nt2

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class HomeFragment : Fragment() {

    // SharedPreferencesに保存するキー
    private val PREFS_NAME = "OmikujiPrefs"
    private val KEY_LAST_DRAW_TIME = "last_draw_time"
    private val KEY_LAST_RESULT = "last_omikuji_result" // ★追加: 最後に引いた結果を保存

    // UIとデータのメンバー変数
    private lateinit var searchEditText: EditText
    private lateinit var btnDrawOmikuji: Button
    private lateinit var textOmikujiResult: TextView

    private lateinit var btnStartAiChat: Button
    private lateinit var sharedPrefs: SharedPreferences

    // おみくじの結果リスト (ファッション運に合わせたメッセージ)
    private val omikujiResults = listOf(
        "大吉！今日は最高のファッション日和です。新しいコーディネートに挑戦しましょう！",
        "吉！素敵な出会いがありそうです。アクセントカラーを身につけましょう。",
        "中吉！着慣れたお気に入りアイテムで安心感を。冒険は控えめに。",
        "小吉！流行を取り入れて気分転換を。小物から挑戦するのが吉。",
        "末吉！無難な色選びが吉と出ます。モノトーンで統一しましょう。",
        "凶…今日は慎重に。地味な色が失敗を避けられます。安全第一！"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // UI要素の初期化
        val btngallery : ImageButton = view.findViewById(R.id.button_my_gallery)
        searchEditText = view.findViewById(R.id.edit_search)
        btnDrawOmikuji = view.findViewById(R.id.btn_draw_omikuji)
        textOmikujiResult = view.findViewById(R.id.text_omikuji_result)
        btnStartAiChat = view.findViewById(R.id.btn_ai_chat)
        // SharedPreferencesの初期化
        sharedPrefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // クリックリスナーの設定
        btngallery.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, GalleryFragment()) // ※ GalleryFragmentが存在することを前提
                .addToBackStack(null)
                .commit()
        }

        btnStartAiChat.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AiChatFragment()) // ※ GalleryFragmentが存在することを前提
                .addToBackStack(null)
                .commit()
        }



        // 各種機能の設定
        setupSearchListeners()
        setupBrandButtons(view)
        setupOmikuji()

        // 初期状態でボタンの有効/無効をチェック (onResumeでも実行される)
        checkOmikujiCooldown()
    }

    /**
     * おみくじ機能に関するロジックを設定する
     */
    private fun setupOmikuji() {
        // おみくじボタンのクリックリスナーを設定
        btnDrawOmikuji.setOnClickListener {
            drawOmikuji()
        }
    }

    /**
     * おみくじを引く処理
     */
    private fun drawOmikuji() {
        // 1. 結果をランダムに決定
        val result = omikujiResults[Random.nextInt(omikujiResults.size)]

        // 2. 画面に表示
        textOmikujiResult.text = "本日のファッション運:\n$result"
        showToast("おみくじを引きました！")

        // 3. 現在の時刻と結果を最終実行時刻として保存
        val currentTime = System.currentTimeMillis()
        sharedPrefs.edit()
            .putLong(KEY_LAST_DRAW_TIME, currentTime)
            .putString(KEY_LAST_RESULT, result) // ★ 結果を保存
            .apply()

        // 4. クールダウン状態に移行し、ボタンを無効化
        checkOmikujiCooldown()
    }

    /**
     * クールダウン状態をチェックし、ボタンの有効/無効と表示を切り替える
     */
    private fun checkOmikujiCooldown() {
        val lastDrawTime = sharedPrefs.getLong(KEY_LAST_DRAW_TIME, 0L)
        val currentTime = System.currentTimeMillis()
        val cooldownPeriod = TimeUnit.HOURS.toMillis(24)

        // 最後に引いた結果を取得
        val lastResult = sharedPrefs.getString(KEY_LAST_RESULT, null)

        if (currentTime - lastDrawTime >= cooldownPeriod) {
            // クールダウン期間が終了している、または初回実行の場合
            btnDrawOmikuji.isEnabled = true
            btnDrawOmikuji.alpha = 1.0f // 不透明に戻す
            btnDrawOmikuji.text = "おみくじを引く"

            // 最後に引いた結果があれば、それを表示
            if (!lastResult.isNullOrBlank()) {
                textOmikujiResult.text = "前回結果:\n$lastResult\n\n今日はもう一度占えます！"
            } else {
                textOmikujiResult.text = "ボタンを押して今日のファッション運を占おう！"
            }

        } else {
            // クールダウン中の場合
            btnDrawOmikuji.isEnabled = false
            btnDrawOmikuji.alpha = 0.5f // 半透明にして無効状態を視覚的に示す

            // 残り時間を計算 (時間と分)
            val elapsed = currentTime - lastDrawTime
            val remainingTimeMillis = cooldownPeriod - elapsed
            val remainingHours = TimeUnit.MILLISECONDS.toHours(remainingTimeMillis)
            val remainingMinutes = TimeUnit.MILLISECONDS.toMinutes(remainingTimeMillis) % 60

            // クールダウン情報を表示
            btnDrawOmikuji.text = "クールダウン中"

            // 最後に引いた結果と残り時間を一緒に表示する
            if (!lastResult.isNullOrBlank()) {
                textOmikujiResult.text = "本日の結果:\n$lastResult\n\n次に引けるまで: ${remainingHours}時間 ${remainingMinutes}分\n(1日1回限定)"
            } else {
                textOmikujiResult.text = "次に引けるまで: ${remainingHours}時間 ${remainingMinutes}分\n(1日1回限定)"
            }
        }
    }

    /**
     * ユーザーが画面に戻ってきたときにもクールダウン状態を再チェック
     */
    override fun onResume() {
        super.onResume()
        // 画面がアクティブになるたびにクールダウン状態を確認
        if (::btnDrawOmikuji.isInitialized) {
            checkOmikujiCooldown()
        }
    }


    /**
     * Webブラウザを開いて検索を実行する
     */
    private fun performWebSearch(brand: String, keyword: String) {
        if (keyword.isBlank()) {
            showToast("検索キーワードを入力してください。")
            return
        }

        // キーワードをURLエンコードする (日本語や特殊文字を含む可能性があるため)
        val encodedKeyword = Uri.encode(keyword)

        // ブランドごとの検索URLを生成
        val baseUrl = when (brand) {
            "GU" -> "https://www.gu-global.com/jp/ja/search?q=$encodedKeyword"
            "ユニクロ" -> "https://www.uniqlo.com/jp/ja/search?q=$encodedKeyword"
            "Amazon" -> "https://www.amazon.co.jp/s?k=$encodedKeyword"
            "ZOZO" -> "https://zozo.jp/search/?p_keyv=$encodedKeyword"
            else -> {
                showToast("無効なブランドです。")
                return
            }
        }

        // Intentを作成し、ブラウザを起動
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(baseUrl))
            startActivity(intent)
            showToast("$brand の「$keyword」を検索します。")
        } catch (e: Exception) {
            showToast("ブラウザを開けませんでした。")
            e.printStackTrace()
        }
    }

    /**
     * 検索欄 (EditText) の機能を設定する (キーボードの検索ボタン)
     */
    private fun setupSearchListeners() {
        // キーボードの「検索」ボタンが押されたときのリスナーを設定
        searchEditText.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                // デフォルトの検索先としてAmazonを使用
                performWebSearch("Amazon", textView.text.toString())
                true
            } else {
                false
            }
        }
    }

    /**
     * ブランドボタンのクリックリスナーを設定する
     */
    private fun setupBrandButtons(view: View) {
        // ブランドのTextViewを取得
        val brandGu = view.findViewById<TextView>(R.id.text_brand_gu)
        val brandUniqlo = view.findViewById<TextView>(R.id.text_brand_uniqlo)
        val brandAmazon = view.findViewById<TextView>(R.id.text_brand_amazon)
        val brandZozo = view.findViewById<TextView>(R.id.text_brand_zozo)

        // クリックリスナーを定義
        val brandClickListener = View.OnClickListener { v ->
            val brandName = (v as TextView).text.toString()
            val keyword = searchEditText.text.toString() // 検索欄のキーワードを取得
            performWebSearch(brandName, keyword)
        }

        // 各TextViewにリスナーを設定
        brandGu.setOnClickListener(brandClickListener)
        brandUniqlo.setOnClickListener(brandClickListener)
        brandAmazon.setOnClickListener(brandClickListener)
        brandZozo.setOnClickListener(brandClickListener)
    }

    /**
     * Toastメッセージを短く表示するヘルパー関数
     */
    private fun showToast(message: String) {
        context?.let {
            Toast.makeText(it, message, Toast.LENGTH_SHORT).show()
        }
    }
}