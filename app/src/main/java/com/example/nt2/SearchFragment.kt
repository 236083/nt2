package com.example.nt2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView

/**
 * 検索画面 (SearchFragment)
 * 1. 天気情報を非同期で取得・表示
 * 2. すべてのカテゴリ/ブランドボタンにクリックリスナーを設定し、Google Mapsアプリを起動
 */
class SearchFragment : Fragment() {

    // --- 天気予報用のUIコンポーネント ---
    private lateinit var weatherIcon: ImageView
    private lateinit var weatherTemp: TextView
    private lateinit var weatherCondition: TextView
    private lateinit var weatherLocation: TextView
    private lateinit var weatherRecommendation: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // fragment_search.xml をインフレート
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        // 1. 天気予報UIコンポーネントのバインド
        bindWeatherViews(view)

        // 2. 検索ボタンのリスナー設定
        setupClickListeners(view)

        // 3. 天気データの取得開始
        fetchWeatherData()

        return view
    }

    /**
     * 天気予報カード内のUI要素をバインドする
     */
    private fun bindWeatherViews(view: View) {
        weatherIcon = view.findViewById(R.id.weather_icon)
        weatherTemp = view.findViewById(R.id.weather_temp)
        weatherCondition = view.findViewById(R.id.weather_condition)
        weatherLocation = view.findViewById(R.id.weather_location)
        weatherRecommendation = view.findViewById(R.id.weather_recommendation)
    }

    /**
     * 天気APIからデータを非同期で取得し、UIを更新する
     *
     * 【重要】
     * 実際のアプリケーションでは、位置情報パーミッションの要求、
     * FusedLocationProviderClientによる現在地の取得、Retrofit/Ktorを使った
     * API通信、そしてKotlin Coroutinesによる非同期処理が必要です。
     * ここでは簡略化のため、ダミーデータとシンプルなThreadを使用します。
     */
    private fun fetchWeatherData() {
        Toast.makeText(requireContext(), "天気情報取得を開始しました。", Toast.LENGTH_SHORT).show()

        // UIをローディング状態に設定
        weatherLocation.text = "現在地を取得中..."
        weatherTemp.text = "--°C"
        weatherCondition.text = "読み込み中"
        weatherRecommendation.text = "しばらくお待ちください..."
        weatherIcon.setImageResource(R.drawable.baseline_cloud_24)

        // 擬似的な非同期処理（実際のネットワーク処理を想定）
        Thread {
            try {
                // 擬似的なネットワーク遅延
                Thread.sleep(2000)

                // 成功時のダミーデータ
                val dummyTemperature = 25
                val dummyCondition = "晴れ"
                val dummyRecommendation = "気温が高いため、薄手の長袖やTシャツが快適です。紫外線対策を忘れずに！"
                val dummyIcon = getIconForCondition(dummyCondition)

                // UIスレッドに戻って更新
                activity?.runOnUiThread {
                    updateWeatherUI(
                        location = "東京 (現在地)", // 実際はGPSで取得
                        temp = "$dummyTemperature°C",
                        condition = dummyCondition,
                        recommendation = dummyRecommendation,
                        iconResId = dummyIcon
                    )
                }

            } catch (e: Exception) {
                // 失敗時の処理
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "天気情報の取得に失敗しました。", Toast.LENGTH_LONG).show()
                    updateWeatherUI(
                        location = "位置情報不明",
                        temp = "--°C",
                        condition = "取得エラー",
                        recommendation = "APIキーまたはパーミッションを確認してください。",
                        iconResId = R.drawable.outline_warning_24 // 警告アイコンを仮定
                    )
                }
            }
        }.start()
    }

    /**
     * 取得した天気データに基づいてUIを更新する
     */
    private fun updateWeatherUI(location: String, temp: String, condition: String, recommendation: String, iconResId: Int) {
        weatherLocation.text = location
        weatherTemp.text = temp
        weatherCondition.text = condition
        weatherRecommendation.text = recommendation
        weatherIcon.setImageResource(iconResId)
        // 必要であればアイコンの色設定も行う
        weatherIcon.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.white))
    }

    /**
     * 天気の状態に基づいて対応するアイコンリソースIDを返す（ダミー）
     * ユーザーはプロジェクトにこれらのDrawableリソースを追加する必要があります。
     */
    private fun getIconForCondition(condition: String): Int {
        return when {
            condition.contains("晴れ") -> R.drawable.baseline_sunny_24
            condition.contains("曇り") -> R.drawable.baseline_cloud_24
            condition.contains("雨") -> R.drawable.outline_rainy_24
            else -> R.drawable.baseline_cloud_24
        }
    }


    /**
     * すべての検索ボタンのクリックリスナーを設定する
     * @param view Fragmentのルートビュー
     */
    private fun setupClickListeners(view: View) {
        // --- カテゴリカードの初期化 ---
        val clothesCard: MaterialCardView = view.findViewById(R.id.card_store_clothes)
        val hairSalonCard: MaterialCardView = view.findViewById(R.id.card_store_hair_salon)
        val accessoriesCard: MaterialCardView = view.findViewById(R.id.card_store_accessories)

        // --- 特定ブランドボタンの初期化 (LinearLayoutのIDを使用) ---
        val uniqloLayout: View = view.findViewById(R.id.layout_brand_uniqlo)
        val guLayout: View = view.findViewById(R.id.layout_brand_gu)
        val zaraLayout: View = view.findViewById(R.id.layout_brand_zara)

        // --- 不足していたブランドボタンの初期化 ---
        val hmLayout: View = view.findViewById(R.id.layout_brand_hm)
        val beamsLayout: View = view.findViewById(R.id.layout_brand_beams)
        val ashLayout: View = view.findViewById(R.id.layout_brand_ash)
        val tbcLayout: View = view.findViewById(R.id.layout_brand_tbc)
        val loftLayout: View = view.findViewById(R.id.layout_brand_loft)
        val mujiLayout: View = view.findViewById(R.id.layout_brand_muji)


        // --- クリックリスナーの設定 ---

        // カテゴリ検索 (isSpecificStore = false)
        clothesCard.setOnClickListener {
            searchNearby("服屋", isSpecificStore = false)
        }

        hairSalonCard.setOnClickListener {
            searchNearby("美容院", isSpecificStore = false)
        }

        accessoriesCard.setOnClickListener {
            searchNearby("服飾雑貨店", isSpecificStore = false)
        }

        // 特定店舗検索 (isSpecificStore = true) - 服屋ブランド
        uniqloLayout.setOnClickListener {
            searchNearby("ユニクロ", isSpecificStore = true)
        }

        guLayout.setOnClickListener {
            searchNearby("GU", isSpecificStore = true)
        }

        zaraLayout.setOnClickListener {
            searchNearby("ZARA", isSpecificStore = true)
        }

        // 追加された特定店舗検索 - 服屋ブランド
        hmLayout.setOnClickListener {
            searchNearby("H&M", isSpecificStore = true)
        }

        beamsLayout.setOnClickListener {
            searchNearby("BEAMS", isSpecificStore = true)
        }

        // 追加された特定店舗検索 - 美容・エステブランド
        ashLayout.setOnClickListener {
            searchNearby("Ash", isSpecificStore = true)
        }

        tbcLayout.setOnClickListener {
            searchNearby("TBC", isSpecificStore = true)
        }

        // 追加された特定店舗検索 - 服飾雑貨・生活雑貨ブランド
        loftLayout.setOnClickListener {
            searchNearby("LOFT", isSpecificStore = true)
        }

        mujiLayout.setOnClickListener {
            searchNearby("MUJI", isSpecificStore = true)
        }
    }


    /**
     * Google Mapsアプリを起動し、現在地周辺の指定されたクエリを検索します。
     * @param query 検索するキーワード（例: "服屋" または "ユニクロ"）
     * @param isSpecificStore 特定の店舗検索かどうか。trueの場合「 店舗」を付加して検索します。
     */
    private fun searchNearby(query: String, isSpecificStore: Boolean) {
        // クエリに「 店舗」を付加するかどうかを決定
        val fullQuery = if (isSpecificStore) {
            "$query 店舗"
        } else {
            query
        }

        // geo:0,0?q=query を使用して、Google Mapsで現在地からの検索をリクエスト
        val gmmIntentUri = Uri.parse("geo:0,0?q=$fullQuery")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)

        // Google Mapsアプリを明示的に指定（推奨）
        mapIntent.setPackage("com.google.android.apps.maps")

        // インテントの解決をチェック
        if (mapIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(mapIntent)
        } else {
            // Google Mapsアプリがない、またはインテントを処理できるアプリがない場合にトースト表示
            Toast.makeText(requireContext(), "Google Mapsアプリが見つかりません。インストールしてください。", Toast.LENGTH_LONG).show()
        }
    }
}