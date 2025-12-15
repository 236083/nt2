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
import androidx.core.content.ContextCompat
import android.text.TextWatcher
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class HomeFragment : Fragment() {

    // SharedPreferencesに保存するキー
    private val PREFS_NAME = "OmikujiPrefs"
    private val KEY_LAST_DRAW_TIME = "last_draw_time"
    private val KEY_LAST_RESULT = "last_omikuji_result"

    // UIとデータのメンバー変数
    private lateinit var searchEditText: EditText
    private lateinit var btnDrawOmikuji: Button
    private lateinit var textOmikujiResult: TextView
    private lateinit var btnStartAiChat: Button
    private lateinit var sharedPrefs: SharedPreferences

    // ★ 修正・追加: 絞り込みボタン
    private lateinit var buttonFilter: ImageButton

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

        // ★ 追加: 絞り込みボタンの初期化
        buttonFilter = view.findViewById(R.id.button_filter)

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
                .replace(R.id.fragment_container, AiChatFragment()) // ※ AiChatFragmentが存在することを前提
                .addToBackStack(null)
                .commit()
        }

        //  絞り込みボタンのクリックリスナー
        buttonFilter.setOnClickListener {
            showCategoryFilterDialog() // カテゴリ選択ダイアログを表示する
        }

        // 各種機能の設定
        setupSearchListeners()
        setupBrandButtons(view) // ★ 修正済み
        setupOmikuji()

        // クリアボタン機能の設定
        setupClearButtonFunctionality()

        // 初期状態でボタンの有効/無効をチェック
        checkOmikujiCooldown()
    }


    private fun setupClearButtonFunctionality() {
        // 1. テキスト変更リスナーの設定 (入力されたらクリアボタンを表示/非表示)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                // テキストが空でなければ、右側にクリアアイコンを設定
                val icon = if (s.isNullOrEmpty()) null else
                    ContextCompat.getDrawable(requireContext(), R.drawable.outline_cancel_24) // ic_clear がない場合は R.drawable.baseline_clear_24 などを使用

                // 右側のDrawableを更新
                searchEditText.setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 2. タッチリスナーの設定 (クリアボタンがタップされたらテキストをクリア)
        searchEditText.setOnTouchListener { v, event ->
            if (event.action == android.view.MotionEvent.ACTION_UP) {
                // DrawableRight (配列のインデックス 2) を取得
                val drawableEnd = searchEditText.compoundDrawables[2]

                if (drawableEnd != null) {
                    val drawableWidth = drawableEnd.bounds.width()

                    // タップ位置 (event.rawX) が EditText の右端からDrawableの領域内にあるかチェック
                    // rawX >= (EditTextの右端 - Drawableの幅 - EditTextの右パディング)
                    if (event.rawX >= (searchEditText.right - drawableWidth - searchEditText.paddingEnd)) {
                        searchEditText.text.clear()
                        // イベントを消費し、EditTextへのデフォルトのタッチ処理を停止
                        return@setOnTouchListener true
                    }
                }
            }
            // Drawableをタップしていない場合は、デフォルトの処理を続行
            false
        }
    }
    // --- おみくじ機能 (変更なし) ---

    private fun setupOmikuji() {
        btnDrawOmikuji.setOnClickListener {
            drawOmikuji()
        }
    }

    private fun drawOmikuji() {
        val result = omikujiResults[Random.nextInt(omikujiResults.size)]
        textOmikujiResult.text = "本日のファッション運:\n$result"
        showToast("おみくじを引きました！")

        val currentTime = System.currentTimeMillis()
        sharedPrefs.edit()
            .putLong(KEY_LAST_DRAW_TIME, currentTime)
            .putString(KEY_LAST_RESULT, result)
            .apply()

        checkOmikujiCooldown()
    }

    private fun checkOmikujiCooldown() {
        val lastDrawTime = sharedPrefs.getLong(KEY_LAST_DRAW_TIME, 0L)
        val currentTime = System.currentTimeMillis()
        val cooldownPeriod = TimeUnit.HOURS.toMillis(24)
        val lastResult = sharedPrefs.getString(KEY_LAST_RESULT, null)

        if (currentTime - lastDrawTime >= cooldownPeriod) {
            btnDrawOmikuji.isEnabled = true
            btnDrawOmikuji.alpha = 1.0f
            btnDrawOmikuji.text = "おみくじを引く"
            if (!lastResult.isNullOrBlank()) {
                textOmikujiResult.text = "前回結果:\n$lastResult\n\n今日はもう一度占えます！"
            } else {
                textOmikujiResult.text = "ボタンを押して今日のファッション運を占おう！"
            }
        } else {
            btnDrawOmikuji.isEnabled = false
            btnDrawOmikuji.alpha = 0.5f
            val elapsed = currentTime - lastDrawTime
            val remainingTimeMillis = cooldownPeriod - elapsed
            val remainingHours = TimeUnit.MILLISECONDS.toHours(remainingTimeMillis)
            val remainingMinutes = TimeUnit.MILLISECONDS.toMinutes(remainingTimeMillis) % 60
            btnDrawOmikuji.text = "クールダウン中"
            if (!lastResult.isNullOrBlank()) {
                textOmikujiResult.text = "本日の結果:\n$lastResult\n\n次に引けるまで: ${remainingHours}時間 ${remainingMinutes}分\n(1日1回限定)"
            } else {
                textOmikujiResult.text = "次に引けるまで: ${remainingHours}時間 ${remainingMinutes}分\n(1日1回限定)"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::btnDrawOmikuji.isInitialized) {
            checkOmikujiCooldown()
        }
    }

    // --- 検索機能 (修正箇所) ---

    /**
     * Webブラウザを開いて検索を実行する (★ 修正: 新規ブランドに対応)
     */
    private fun performWebSearch(brand: String, keyword: String) {
        if (keyword.isBlank()) {
            showToast("検索キーワードを入力してください。")
            return
        }

        val encodedKeyword = Uri.encode(keyword)

        // ブランドごとの検索URLを生成
        val baseUrl = when (brand) {
            "GU" -> "https://www.gu-global.com/jp/ja/search?q=$encodedKeyword"
            "ユニクロ" -> "https://www.uniqlo.com/jp/ja/search?q=$encodedKeyword"
            "Amazon" -> "https://www.amazon.co.jp/s?k=$encodedKeyword"
            "ZOZO" -> "https://zozo.jp/category/$encodedKeyword/"
            "楽天" -> "https://brandavenue.rakuten.co.jp/all-sites/item/?l-id=brn_searchmenu_filter_keyword&free_word=$encodedKeyword&inventory_flg=1"
            "GRL" -> "https://www.grail.bz/disp/tagitemlist/?tag=$encodedKeyword"
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
     * 検索欄 (EditText) の機能を設定する
     */
    private fun setupSearchListeners() {
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
     * ブランドボタンのクリックリスナーを設定する (★ 修正: 新規ブランドに対応)
     */
    private fun setupBrandButtons(view: View) {
        // 既存のブランドのTextViewを取得
        val brandGu = view.findViewById<TextView>(R.id.text_brand_gu)
        val brandUniqlo = view.findViewById<TextView>(R.id.text_brand_uniqlo)
        val brandAmazon = view.findViewById<TextView>(R.id.text_brand_amazon)
        val brandZozo = view.findViewById<TextView>(R.id.text_brand_zozo)

        // ★ 追加ブランドのTextViewを取得
        val brandRakuten = view.findViewById<TextView>(R.id.text_brand_rakuten)
        val brandGrl = view.findViewById<TextView>(R.id.text_brand_grl)


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
        brandRakuten.setOnClickListener(brandClickListener)
        brandGrl.setOnClickListener(brandClickListener)
    }

    /**
     * カテゴリ選択用のダイアログを表示する (★ 新規追加)
     * ユーザーがトップスやパンツなどのカテゴリを選択し、検索キーワードに追加する
     */
    private fun showCategoryFilterDialog() {
        // カテゴリリスト。ここに性別フィルターを追加することも可能
        val categories = arrayOf("トップス", "アウター", "パンツ", "スカート", "シューズ", "バッグ", "全てクリア")

        // アラートダイアログビルダーを使用
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("カテゴリで絞り込む")

        // 選択されたアイテムを一時的に保持するSet
        val selectedItems = mutableSetOf<String>()

        // 複数選択リストを設定
        builder.setMultiChoiceItems(categories, null) { dialog, which, isChecked ->
            val category = categories[which]

            // 「全てクリア」が選択されたら他の選択を解除し、リストのチェックをクリア
            if (category == "全てクリア") {
                if (isChecked) {
                    selectedItems.clear()
                    // ダイアログの選択肢をクリアするために、一度ダイアログを閉じるか、カスタムビューを使用する必要がある
                    // 簡単な実装では、この操作をキャンセルボタンに割り当てることが多いが、今回はリスト内操作として簡略化
                }
            } else {
                if (isChecked) {
                    selectedItems.add(category)
                } else {
                    selectedItems.remove(category)
                }
            }
        }

        // 確定ボタン
        builder.setPositiveButton("検索キーワードに設定") { dialog, id ->
            if (selectedItems.isNotEmpty()) {
                val categoryString = selectedItems.joinToString(" ")

                // 既存のキーワードの末尾にカテゴリを追加
                val currentKeyword = searchEditText.text.toString().trim()

                // 検索欄にセット
                searchEditText.setText(if (currentKeyword.isBlank()) categoryString else "$currentKeyword $categoryString")
                showToast("カテゴリ: $categoryString を検索キーワードに追加しました。")
            } else {
                showToast("カテゴリが選択されていません。")
            }
        }

        // キャンセルボタン
        builder.setNegativeButton("キャンセル") { dialog, id ->
            dialog.cancel()
        }

        builder.create().show()
    }

    // --- その他 (変更なし) ---
    private fun showToast(message: String) {
        context?.let {
            Toast.makeText(it, message, Toast.LENGTH_SHORT).show()
        }
    }
}