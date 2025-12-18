package com.example.nt2

import android.app.Activity
import android.app.AlertDialog
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
import android.provider.MediaStore
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import android.view.MotionEvent
import android.content.res.ColorStateList
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class HomeFragment : Fragment() {

    private val PREFS_NAME = "OmikujiPrefs"
    private val KEY_LAST_DRAW_TIME = "last_draw_time"
    private val KEY_LAST_RESULT = "last_omikuji_result"

    private val REQUEST_IMAGE_CAPTURE = 1
    private val PERMISSION_REQUEST_CODE = 100

    private lateinit var searchEditText: EditText
    private lateinit var btnDrawOmikuji: Button
    private lateinit var textOmikujiResult: TextView
    private lateinit var textCooldownTimer: TextView // 次回までの時間を表示用
    private lateinit var btnStartAiChat: Button
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var buttonFilter: ImageButton
    private lateinit var cameraButton: ImageButton

    private val omikujiResults = listOf(
        "大吉！今日は最高のファッション日和です。新しいコーディネートに挑戦しましょう！",
        "吉！素敵な出会いがありそうです。アクセントカラーを身につけましょう。",
        "中吉！着慣れたお気に入りアイテムで安心感を。冒険は控えめに。",
        "小吉！流行を取り入れて気分転換を。小物から挑戦するのが吉。",
        "末吉！無難な色選びが吉と出ます。モノトーンで統一しましょう。",
        "凶…今日は慎重に。地味な色が失敗を避けられます。安全第一！"
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btngallery : ImageButton = view.findViewById(R.id.button_my_gallery)
        searchEditText = view.findViewById(R.id.edit_search)
        btnDrawOmikuji = view.findViewById(R.id.btn_draw_omikuji)
        textOmikujiResult = view.findViewById(R.id.text_omikuji_result)
        textCooldownTimer = view.findViewById(R.id.text_cooldown_timer) // XMLにこのIDが必要です
        btnStartAiChat = view.findViewById(R.id.btn_ai_chat)
        buttonFilter = view.findViewById(R.id.button_filter)
        cameraButton = view.findViewById(R.id.camera_button)

        sharedPrefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        btngallery.setOnClickListener {
            parentFragmentManager.beginTransaction().replace(R.id.fragment_container, GalleryFragment()).addToBackStack(null).commit()
        }
        btnStartAiChat.setOnClickListener {
            parentFragmentManager.beginTransaction().replace(R.id.fragment_container, AiChatFragment()).addToBackStack(null).commit()
        }
        buttonFilter.setOnClickListener { showCategoryFilterDialog() }
        cameraButton.setOnClickListener { checkCameraPermissionAndDispatch() }

        // おみくじ結果表示部分のクリックイベント（詳細ポップアップ）
        textOmikujiResult.setOnClickListener {
            val lastResult = sharedPrefs.getString(KEY_LAST_RESULT, null)
            if (!lastResult.isNullOrBlank()) {
                showFashionFortuneDialog(lastResult)
            }
        }

        setupSearchListeners()
        setupBrandButtons(view)
        setupOmikuji()
        setupClearButtonFunctionality()
        checkOmikujiCooldown()
    }

    // --- おみくじ関連 ---
    private fun setupOmikuji() {
        btnDrawOmikuji.setOnClickListener {
            drawOmikuji()
        }
    }

    private fun drawOmikuji() {
        val res = omikujiResults[Random.nextInt(omikujiResults.size)]
        // 結果のテキスト更新（詳細への誘導を追加）
        textOmikujiResult.text = "本日の結果: ${res.substringBefore("！")}\n(タップで詳細を表示)"

        showFashionFortuneDialog(res)

        sharedPrefs.edit()
            .putLong(KEY_LAST_DRAW_TIME, System.currentTimeMillis())
            .putString(KEY_LAST_RESULT, res)
            .apply()

        checkOmikujiCooldown()
    }

    private fun checkOmikujiCooldown() {
        val lastDrawTime = sharedPrefs.getLong(KEY_LAST_DRAW_TIME, 0L)
        val lastResult = sharedPrefs.getString(KEY_LAST_RESULT, "")
        val currentTime = System.currentTimeMillis()
        val cooldownPeriod = 86400000L // 24時間

        if (currentTime - lastDrawTime >= cooldownPeriod) {
            // 占い可能な状態
            btnDrawOmikuji.isEnabled = true
            btnDrawOmikuji.alpha = 1.0f
            btnDrawOmikuji.text = "占う"
            textCooldownTimer.visibility = View.GONE

            if (lastResult.isNullOrEmpty()) {
                textOmikujiResult.text = "今日の運勢を占ってみよう！"
            } else {
                textOmikujiResult.text = "前回の結果: ${lastResult.substringBefore("！")}\nもう一度占えます！"
            }
        } else {
            // 占い済み（クールダウン中）
            btnDrawOmikuji.isEnabled = false
            btnDrawOmikuji.alpha = 0.5f
            btnDrawOmikuji.text = "済"

            // 残り時間の計算
            val timeLeft = cooldownPeriod - (currentTime - lastDrawTime)
            val hours = TimeUnit.MILLISECONDS.toHours(timeLeft)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(timeLeft) % 60

            textCooldownTimer.visibility = View.VISIBLE
            textCooldownTimer.text = "次まであと: ${hours}時間${minutes}分"

            if (!lastResult.isNullOrEmpty()) {
                textOmikujiResult.text = "本日の結果: ${lastResult.substringBefore("！")}\n(タップで詳細を表示)"
            }
        }
    }

    private fun showFashionFortuneDialog(msg: String) {
        val v = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_fashion_fortune, null)
        val d = AlertDialog.Builder(requireContext()).setView(v).create()
        v.findViewById<TextView>(R.id.dialog_fortune_level).text = msg.substringBefore("！")
        v.findViewById<TextView>(R.id.dialog_message).text = msg
        v.findViewById<Button>(R.id.dialog_button).setOnClickListener { d.dismiss() }
        d.window?.setBackgroundDrawableResource(android.R.color.transparent)
        d.show()
    }

    // --- カテゴリ詳細フィルタ ---
    private fun showCategoryFilterDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_category_filter, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()

        val chipGroup = dialogView.findViewById<ChipGroup>(R.id.chip_group_categories)
        val btnClear = dialogView.findViewById<Button>(R.id.btn_filter_clear)
        val btnApply = dialogView.findViewById<Button>(R.id.btn_filter_apply)

        val categories = listOf(
            "トップス", "シャツ", "Tシャツ", "ニット", "アウター", "コート", "ジャケット",
            "パンツ", "デニム", "スラックス", "スカート", "ワンピース",
            "シューズ", "スニーカー", "パンプス", "バッグ", "帽子", "アクセサリー", "時計", "ベルト",
            "韓国コスメ"
        )

        val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
        val bgColors = intArrayOf(ContextCompat.getColor(requireContext(), android.R.color.black), ContextCompat.getColor(requireContext(), android.R.color.white))
        val textColors = intArrayOf(ContextCompat.getColor(requireContext(), android.R.color.white), ContextCompat.getColor(requireContext(), android.R.color.black))

        categories.forEach { categoryName ->
            val chip = Chip(requireContext()).apply {
                text = categoryName
                isCheckable = true
                isCheckedIconVisible = false
                chipStrokeWidth = 2f
                chipStrokeColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
                chipBackgroundColor = ColorStateList(states, bgColors)
                setTextColor(ColorStateList(states, textColors))
                rippleColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
            }
            chipGroup.addView(chip)
        }

        btnClear.setOnClickListener {
            chipGroup.clearCheck()
            searchEditText.text.clear()
            dialog.dismiss()
        }

        btnApply.setOnClickListener {
            val selected = mutableListOf<String>()
            for (i in 0 until chipGroup.childCount) {
                val chip = chipGroup.getChildAt(i) as Chip
                if (chip.isChecked) selected.add(chip.text.toString())
            }
            if (selected.isNotEmpty()) searchEditText.setText(selected.joinToString(" "))
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    // --- 検索ロジック ---
    private fun performWebSearch(brand: String, keyword: String) {
        if (keyword.isBlank()) {
            showToast("検索キーワードを入力してください")
            return
        }
        val encodedKeyword = Uri.encode(keyword)
        val url = when (brand) {
            "GU" -> "https://www.gu-global.com/jp/ja/search?q=$encodedKeyword"
            "ユニクロ" -> "https://www.uniqlo.com/jp/ja/search?q=$encodedKeyword"
            "Amazon" -> "https://www.amazon.co.jp/s?k=$encodedKeyword"
            "ZOZO" -> "https://zozo.jp/sp/search/?p_keyv=$encodedKeyword"
            "楽天" -> "https://brandavenue.rakuten.co.jp/all-sites/item/?l-id=brn_searchmenu_filter_keyword&free_word=$encodedKeyword&inventory_flg=1"
            "GRL" -> "https://www.grail.bz/disp/tagitemlist/?tag=$encodedKeyword"
            else -> "https://www.google.com/search?q=${Uri.encode("$brand $keyword")}"
        }
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            showToast("ブラウザを開けませんでした")
        }
    }

    private fun setupSearchListeners() {
        searchEditText.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performWebSearch("Amazon", v.text.toString())
                true
            } else false
        }
    }

    private fun setupBrandButtons(view: View) {
        val brandIds = listOf(R.id.text_brand_gu, R.id.text_brand_uniqlo, R.id.text_brand_amazon, R.id.text_brand_zozo, R.id.text_brand_rakuten, R.id.text_brand_grl)
        brandIds.forEach { id ->
            view.findViewById<TextView>(id)?.setOnClickListener { v ->
                performWebSearch((v as TextView).text.toString(), searchEditText.text.toString())
            }
        }
    }

    private fun showToast(message: String) = Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

    private fun checkCameraPermissionAndDispatch() {
        val permission = android.Manifest.permission.CAMERA
        if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(permission), PERMISSION_REQUEST_CODE)
        } else {
            dispatchTakePictureIntent()
        }
    }

    private fun dispatchTakePictureIntent() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try { startActivityForResult(intent, REQUEST_IMAGE_CAPTURE) }
        catch (e: Exception) { showToast("カメラを起動できません") }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            parentFragmentManager.beginTransaction().replace(R.id.fragment_container, GalleryFragment()).commit()
        }
    }

    private fun setupClearButtonFunctionality() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                val icon = if (s.isNullOrEmpty()) null else ContextCompat.getDrawable(requireContext(), R.drawable.outline_cancel_24)
                searchEditText.setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        searchEditText.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val d = searchEditText.compoundDrawables[2]
                if (d != null && event.rawX >= (searchEditText.right - d.bounds.width() - searchEditText.paddingEnd)) {
                    searchEditText.text.clear()
                    return@setOnTouchListener true
                }
            }
            false
        }
    }
}