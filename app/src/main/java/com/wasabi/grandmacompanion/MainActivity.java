package com.wasabi.grandmacompanion;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity {
    private static final String[] API_URLS = {
            "https://api.minimaxi.com/v1/chat/completions",
            "https://api.minimax.io/v1/chat/completions"
    };
    private static final String NEWS_RSS_URL = "https://www.chinanews.com.cn/rss/scroll-news.xml";
    private static final String DEFAULT_MODEL = "MiniMax-M2";
    private static final String PREFS = "grandma_companion_prefs";
    private static final String KEY_API = "minimax_api_key";
    private static final String KEY_MESSAGES = "messages";
    private static final String KEY_MEMORY_NOTES = "memory_notes";
    private static final String KEY_MEMORY_USER_COUNT = "memory_user_count";
    private static final String KEY_RECOMMENDATION_DATE = "recommendation_date_";
    private static final String KEY_RECOMMENDATION_TEXT = "recommendation_text_";
    private static final String KEY_RECOMMENDATION_STYLE_VERSION = "recommendation_style_version";
    private static final String KEY_PROMPT_STYLE_VERSION = "prompt_style_version";
    private static final String KEY_TOOLBOX_DATE = "toolbox_date";
    private static final String KEY_TOOLBOX_MOOD = "toolbox_mood";
    private static final String KEY_TOOLBOX_GRATITUDE = "toolbox_gratitude";
    private static final String KEY_TOOLBOX_ACTION = "toolbox_action";
    private static final int PROMPT_STYLE_VERSION = 2;
    private static final int RECOMMENDATION_STYLE_VERSION = 2;
    private static final int MAX_HISTORY_MESSAGES = 12;
    private static final int MAX_STORED_MESSAGES = 60;
    private static final int MEMORY_UPDATE_EVERY_USER_MESSAGES = 4;
    private static final String[] FUN_FALLBACKS = {
            "今天的开心一下：\n\n1. 早上出门买菜，走到楼下才想起来没拿篮子。回家一看，篮子在门口等我，比我还稳当。\n\n2. 有人问：怎么才能睡得香？答：白天少操心，晚上被子别漏风。\n\n3. 今天也算有收获，至少把昨天没想明白的事，成功留给了明天。",
            "今天的开心一下：\n\n1. 菜市场最会聊天的人，不是买菜的，是那棵被大家挑来挑去还很精神的白菜。\n\n2. 老朋友见面，一句“你还和以前一样”，就够人高兴半天。\n\n3. 天气好时晒晒太阳，天气不好时晒晒被子，日子总有一面能晒到。",
            "今天的开心一下：\n\n1. 有人说养花要有耐心。花说：你先别天天问我怎么还不开。\n\n2. 做饭最神奇的地方是，明明只是多放了一点盐，全家人都能尝出来。\n\n3. 小日子不用太热闹，有茶有灯，有人说两句话，就挺好。"
    };
    private static final String[] WISDOM_FALLBACKS = {
            "心灵鸡汤：\n\n1. 苏轼\n“回首向来萧瑟处，归去，也无风雨也无晴。”\n\n苏轼一生多次被贬，日子并不顺，可他在黄州种地、写诗、做东坡肉，把困境过成了另一种生活。人的路有时绕远了，但只要心还愿意看见一点光，就不算白走。\n\n2. 海伦·凯勒\n“只要朝着阳光，便不会看见阴影。”\n\n海伦·凯勒很小就失去视力和听力，却在老师帮助下学习说话、读书，后来写作、演讲，鼓励了许多人。她的故事不是说人必须坚强，而是提醒我们：哪怕条件很难，也可以先抓住今天能做的一小步。",
            "心灵鸡汤：\n\n1. 司马迁\n“人固有一死，或重于泰山，或轻于鸿毛。”\n\n司马迁遭遇巨大屈辱后，仍忍着痛写完《史记》。他把个人苦难放进更长的时间里，留下了后人能读见的历史。很多时候，撑住不是为了证明自己多厉害，而是为了不让重要的事半途而废。\n\n2. 林肯\n“我走得很慢，但我从不后退。”\n\n林肯年轻时经历过多次失败，做生意失败，竞选也失败过，但他没有把一次失败当成一生的结论。日子慢一点没关系，只要没有放弃往前挪，人生就还在变化。",
            "心灵鸡汤：\n\n1. 屠呦呦\n“不要去追一匹马，用追马的时间种草。”\n\n屠呦呦和团队多年查阅古籍、反复实验，最终发现青蒿素，帮助许多疟疾患者。真正有价值的事，常常不是一下子热闹起来，而是在安静里一点点积累出来。\n\n2. 曼德拉\n“生命中最伟大的荣耀不在于从不跌倒，而在于每次跌倒后都能站起来。”\n\n曼德拉经历漫长牢狱生活，后来仍选择推动和解。一个人受过苦，不一定要让心变硬；能慢慢放下怨气，本身也是一种很深的力量。"
    };

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final List<ChatMessage> messages = new ArrayList<>();
    private LinearLayout chatList;
    private ScrollView scrollView;
    private TextView moodBadge;
    private LinearLayout chatLoadingPill;
    private BouncingDotsView chatProgress;
    private TextView chatProgressText;
    private EditText input;
    private LinearLayout bottomInputPanel;
    private Button sendButton;
    private Button recommendationNewsButton;
    private Button recommendationFunButton;
    private Button recommendationWisdomButton;
    private BouncingDotsView recommendationProgress;
    private TextView recommendationOutput;
    private boolean showingRecommendations = false;
    private boolean showingToolbox = false;
    private TextView toolboxStatusText;
    private BreathCircleView breathCircle;
    private EditText gratitudeInput;
    private EditText gentleActionInput;
    private int selectedMood = 2;
    private int keyboardLiftPx = 0;
    private String lastAssistantText = "";
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        migrateRecommendationStyleIfNeeded();
        buildUi();
        migratePromptStyleIfNeeded();
        loadMessages();
        if (messages.isEmpty()) {
            addAssistantMessage("我在。你可以随便说点什么，不用整理得很完整。今天如果只想聊两句，也完全可以。");
        } else {
            renderMessages();
        }
    }

    private void buildUi() {
        resetWindowAnimationState();
        keyboardLiftPx = 0;
        int pad = dp(16);
        FrameLayout frame = new FrameLayout(this);
        frame.setBackgroundResource(getDrawableId("bg_app_background"));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad + dp(20), pad + getStatusBarHeight(), pad + dp(20), pad + getNavigationBarHeight());
        root.setBackgroundColor(Color.TRANSPARENT);

        TextView title = new TextView(this);
        title.setText("安心伴");
        title.setTextSize(31);
        title.setTextColor(getColorRes("ink"));
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        root.addView(title, new LinearLayout.LayoutParams(-1, dp(42)));

        TextView subtitle = new TextView(this);
        subtitle.setText("像朋友一样聊聊，也给今天留一点轻松");
        subtitle.setTextSize(16);
        subtitle.setTextColor(getColorRes("muted_ink"));
        subtitle.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        root.addView(subtitle, new LinearLayout.LayoutParams(-1, dp(32)));

        moodBadge = new TextView(this);
        moodBadge.setText("今天也慢慢来  ·  有话就说，没话也没关系");
        moodBadge.setTextSize(15);
        moodBadge.setTextColor(getColorRes("accent_dark"));
        moodBadge.setGravity(Gravity.CENTER_VERTICAL);
        moodBadge.setPadding(dp(14), 0, dp(14), 0);
        moodBadge.setBackgroundResource(getDrawableId("bg_mood_badge"));
        LinearLayout.LayoutParams moodParams = new LinearLayout.LayoutParams(-1, dp(42));
        moodParams.topMargin = dp(4);
        moodParams.bottomMargin = dp(6);
        root.addView(moodBadge, moodParams);
        startGentlePulse(moodBadge);

        LinearLayout quickActions = new LinearLayout(this);
        quickActions.setOrientation(LinearLayout.VERTICAL);
        quickActions.setPadding(0, dp(4), 0, dp(10));
        root.addView(quickActions, new LinearLayout.LayoutParams(-1, dp(112)));

        LinearLayout firstActionRow = new LinearLayout(this);
        firstActionRow.setOrientation(LinearLayout.HORIZONTAL);
        quickActions.addView(firstActionRow, new LinearLayout.LayoutParams(-1, 0, 1));

        Button keyButton = secondaryButton("设置密钥");
        keyButton.setOnClickListener(v -> showKeyDialog());
        firstActionRow.addView(keyButton, new LinearLayout.LayoutParams(0, -1, 1));

        Button dailyButton = secondaryButton("每日推荐");
        dailyButton.setOnClickListener(v -> openRecommendationUi());
        LinearLayout.LayoutParams dailyParams = new LinearLayout.LayoutParams(0, -1, 1);
        dailyParams.leftMargin = dp(10);
        firstActionRow.addView(dailyButton, dailyParams);

        LinearLayout secondActionRow = new LinearLayout(this);
        secondActionRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams secondRowParams = new LinearLayout.LayoutParams(-1, 0, 1);
        secondRowParams.topMargin = dp(8);
        quickActions.addView(secondActionRow, secondRowParams);

        Button memoryButton = secondaryButton("记忆");
        memoryButton.setOnClickListener(v -> showMemoryDialog());
        secondActionRow.addView(memoryButton, new LinearLayout.LayoutParams(0, -1, 1));

        Button toolboxButton = secondaryButton("安心工具");
        toolboxButton.setOnClickListener(v -> openToolboxUi());
        LinearLayout.LayoutParams toolboxParams = new LinearLayout.LayoutParams(0, -1, 1);
        toolboxParams.leftMargin = dp(10);
        secondActionRow.addView(toolboxButton, toolboxParams);

        scrollView = new ScrollView(this);
        scrollView.setClipToPadding(false);
        chatList = new LinearLayout(this);
        chatList.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(chatList);
        root.addView(scrollView, new LinearLayout.LayoutParams(-1, 0, 1));

        bottomInputPanel = new LinearLayout(this);
        bottomInputPanel.setOrientation(LinearLayout.VERTICAL);
        root.addView(bottomInputPanel, new LinearLayout.LayoutParams(-1, -2));

        chatLoadingPill = new LinearLayout(this);
        chatLoadingPill.setOrientation(LinearLayout.HORIZONTAL);
        chatLoadingPill.setGravity(Gravity.CENTER);
        chatLoadingPill.setBackgroundResource(getDrawableId("bg_loading_pill"));
        chatLoadingPill.setAlpha(0f);
        chatLoadingPill.setVisibility(View.INVISIBLE);

        chatProgress = new BouncingDotsView(this);
        chatLoadingPill.addView(chatProgress, new LinearLayout.LayoutParams(dp(64), dp(30)));

        chatProgressText = new TextView(this);
        chatProgressText.setText("正在整理一句合适的话");
        chatProgressText.setTextSize(14);
        chatProgressText.setTextColor(getColorRes("muted_ink"));
        chatProgressText.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams progressTextParams = new LinearLayout.LayoutParams(-2, -1);
        progressTextParams.leftMargin = dp(6);
        chatLoadingPill.addView(chatProgressText, progressTextParams);

        LinearLayout.LayoutParams chatProgressParams = new LinearLayout.LayoutParams(-2, dp(42));
        chatProgressParams.gravity = Gravity.CENTER_HORIZONTAL;
        chatProgressParams.topMargin = dp(2);
        chatProgressParams.bottomMargin = dp(2);
        bottomInputPanel.addView(chatLoadingPill, chatProgressParams);

        LinearLayout composer = new LinearLayout(this);
        composer.setOrientation(LinearLayout.HORIZONTAL);
        composer.setGravity(Gravity.BOTTOM);
        composer.setPadding(0, dp(8), 0, 0);
        bottomInputPanel.addView(composer, new LinearLayout.LayoutParams(-1, dp(76)));

        input = new EditText(this);
        input.setTextSize(18);
        input.setHint("点这里写字，说点什么都可以");
        input.setTextColor(getColorRes("ink"));
        input.setHintTextColor(getColorRes("muted_ink"));
        input.setBackgroundResource(getDrawableId("bg_input_field"));
        input.setPadding(dp(16), 0, dp(16), 0);
        input.setMinLines(1);
        input.setMaxLines(3);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setOnClickListener(v -> {
            showSoftKeyboard();
        });
        input.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showSoftKeyboard();
            }
        });
        composer.addView(input, new LinearLayout.LayoutParams(0, -1, 1));

        sendButton = primaryButton("发送");
        sendButton.setOnClickListener(v -> sendCurrentText());
        LinearLayout.LayoutParams sendParams = new LinearLayout.LayoutParams(dp(84), -1);
        sendParams.leftMargin = dp(8);
        composer.addView(sendButton, sendParams);

        frame.addView(root, new FrameLayout.LayoutParams(-1, -1));

        FloatingMascotView leftDecor = new FloatingMascotView(this, false);
        FrameLayout.LayoutParams leftParams = new FrameLayout.LayoutParams(dp(38), -1);
        leftParams.gravity = Gravity.LEFT;
        leftParams.leftMargin = dp(2);
        frame.addView(leftDecor, leftParams);

        FloatingMascotView rightDecor = new FloatingMascotView(this, true);
        FrameLayout.LayoutParams rightParams = new FrameLayout.LayoutParams(dp(38), -1);
        rightParams.gravity = Gravity.RIGHT;
        rightParams.rightMargin = dp(2);
        frame.addView(rightDecor, rightParams);

        setContentView(frame);
        installKeyboardLiftListener(frame);
        animateScreenIn(frame);
    }

    private Button primaryButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(18);
        button.setTextColor(Color.WHITE);
        button.setAllCaps(false);
        button.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        button.setElevation(dp(2));
        button.setBackgroundResource(getDrawableId("bg_button_primary"));
        addPressAnimation(button);
        return button;
    }

    private Button secondaryButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(17);
        button.setTextColor(getColorRes("ink"));
        button.setAllCaps(false);
        button.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        button.setElevation(dp(1));
        button.setBackgroundResource(getDrawableId("bg_button_secondary"));
        addPressAnimation(button);
        return button;
    }

    private void addPressAnimation(View view) {
        ButtonMotion.bind(view);
    }

    private void startGentlePulse(View view) {
        AlphaAnimation animation = new AlphaAnimation(0.96f, 1f);
        animation.setDuration(4000);
        animation.setRepeatMode(Animation.REVERSE);
        animation.setRepeatCount(Animation.INFINITE);
        view.startAnimation(animation);
    }

    private void animateBubble(View view, boolean isUser) {
        AlphaAnimation fade = new AlphaAnimation(0f, 1f);
        TranslateAnimation slide = new TranslateAnimation(
                isUser ? dp(9) : -dp(9),
                0,
                dp(4),
                0);
        AnimationSet set = new AnimationSet(true);
        set.addAnimation(fade);
        set.addAnimation(slide);
        set.setDuration(280);
        set.setInterpolator(new DecelerateInterpolator());
        view.startAnimation(set);
    }

    private void animatePop(View view, long delayMs) {
        if (view == null) {
            return;
        }
        view.setTranslationY(dp(6));
        view.setAlpha(0f);
        view.animate()
                .alpha(1f)
                .translationY(0)
                .setStartDelay(delayMs)
                .setDuration(240)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private void animateScreenIn(View view) {
        view.setAlpha(0f);
        view.setTranslationY(dp(10));
        view.animate()
                .alpha(1f)
                .translationY(0)
                .setDuration(220)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private void openRecommendationUi() {
        View current = getCurrentContentView();
        current.animate()
                .alpha(0f)
                .translationY(-dp(6))
                .setDuration(120)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> {
                    resetViewAnimationState(current);
                    buildRecommendationUi();
                })
                .start();
    }

    private void openToolboxUi() {
        View current = getCurrentContentView();
        current.animate()
                .alpha(0f)
                .translationY(-dp(6))
                .setDuration(120)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> {
                    resetViewAnimationState(current);
                    buildToolboxUi();
                })
                .start();
    }

    private void buildRecommendationUi() {
        resetWindowAnimationState();
        bottomInputPanel = null;
        keyboardLiftPx = 0;
        showingRecommendations = true;
        showingToolbox = false;
        int pad = dp(16);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad + getStatusBarHeight(), pad, pad + getNavigationBarHeight());
        root.setBackgroundResource(getDrawableId("bg_app_background"));

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(top, new LinearLayout.LayoutParams(-1, dp(58)));

        Button backButton = secondaryButton("返回");
        backButton.setOnClickListener(v -> returnToChat());
        LinearLayout.LayoutParams backParams = new LinearLayout.LayoutParams(dp(92), dp(48));
        top.addView(backButton, backParams);

        TextView title = new TextView(this);
        title.setText("每日推荐");
        title.setTextSize(26);
        title.setTextColor(getColorRes("ink"));
        title.setTypeface(null, 1);
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setSingleLine(true);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, -1, 1);
        titleParams.leftMargin = dp(12);
        top.addView(title, titleParams);

        TextView subtitle = new TextView(this);
        subtitle.setText("换个频道，看一点轻松的东西");
        subtitle.setTextSize(16);
        subtitle.setTextColor(getColorRes("muted_ink"));
        subtitle.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(-1, -2);
        subtitleParams.topMargin = dp(2);
        subtitleParams.bottomMargin = dp(12);
        root.addView(subtitle, subtitleParams);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setPadding(0, 0, 0, 0);
        LinearLayout.LayoutParams actionsParams = new LinearLayout.LayoutParams(-1, -2);
        actionsParams.bottomMargin = dp(12);
        root.addView(actions, actionsParams);

        recommendationNewsButton = primaryButton("今日新闻");
        recommendationNewsButton.setOnClickListener(v -> loadRecommendation("news"));
        actions.addView(recommendationNewsButton, new LinearLayout.LayoutParams(0, dp(52), 1));

        recommendationFunButton = secondaryButton("开心一下");
        recommendationFunButton.setBackgroundResource(getDrawableId("bg_button_fun"));
        recommendationFunButton.setOnClickListener(v -> loadRecommendation("fun"));
        LinearLayout.LayoutParams funParams = new LinearLayout.LayoutParams(0, dp(52), 1);
        funParams.leftMargin = dp(8);
        actions.addView(recommendationFunButton, funParams);

        recommendationWisdomButton = secondaryButton("心灵鸡汤");
        recommendationWisdomButton.setBackgroundResource(getDrawableId("bg_button_wisdom"));
        recommendationWisdomButton.setOnClickListener(v -> loadRecommendation("wisdom"));
        LinearLayout.LayoutParams wisdomParams = new LinearLayout.LayoutParams(0, dp(52), 1);
        wisdomParams.leftMargin = dp(8);
        actions.addView(recommendationWisdomButton, wisdomParams);

        recommendationProgress = new BouncingDotsView(this);
        recommendationProgress.setVisibility(View.GONE);
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(dp(72), dp(30));
        progressParams.gravity = Gravity.CENTER_HORIZONTAL;
        progressParams.topMargin = dp(2);
        progressParams.bottomMargin = dp(10);
        root.addView(recommendationProgress, progressParams);

        ScrollView recommendationScroll = new ScrollView(this);
        recommendationScroll.setFillViewport(false);
        recommendationOutput = new TextView(this);
        recommendationOutput.setText(getRecommendationWelcomeText());
        recommendationOutput.setTextSize(20);
        recommendationOutput.setLineSpacing(dp(4), 1.22f);
        recommendationOutput.setTextColor(getColorRes("ink"));
        recommendationOutput.setPadding(dp(20), dp(18), dp(20), dp(18));
        recommendationOutput.setBackgroundResource(getDrawableId("bg_bubble_ai"));
        ScrollView.LayoutParams outputParams = new ScrollView.LayoutParams(-1, -2);
        recommendationScroll.addView(recommendationOutput, outputParams);
        root.addView(recommendationScroll, new LinearLayout.LayoutParams(-1, 0, 1));

        setContentView(root);
        animateScreenIn(root);
        animatePop(recommendationNewsButton, 40);
        animatePop(recommendationFunButton, 80);
        animatePop(recommendationWisdomButton, 120);
        animateRecommendationOutput();
    }

    private void buildToolboxUi() {
        resetWindowAnimationState();
        bottomInputPanel = null;
        keyboardLiftPx = 0;
        showingRecommendations = false;
        showingToolbox = true;
        int pad = dp(16);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad + getStatusBarHeight(), pad, pad + getNavigationBarHeight());
        root.setBackgroundResource(getDrawableId("bg_app_background"));

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(top, new LinearLayout.LayoutParams(-1, dp(58)));

        Button backButton = secondaryButton("返回");
        backButton.setOnClickListener(v -> returnToChat());
        top.addView(backButton, new LinearLayout.LayoutParams(dp(92), dp(48)));

        TextView title = new TextView(this);
        title.setText("安心工具");
        title.setTextSize(26);
        title.setTextColor(getColorRes("ink"));
        title.setTypeface(null, 1);
        title.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, -1, 1);
        titleParams.leftMargin = dp(12);
        top.addView(title, titleParams);

        TextView subtitle = new TextView(this);
        subtitle.setText("不想聊天时，也可以先照顾这一小会儿");
        subtitle.setTextSize(16);
        subtitle.setTextColor(getColorRes("muted_ink"));
        subtitle.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(-1, -2);
        subtitleParams.topMargin = dp(2);
        subtitleParams.bottomMargin = dp(12);
        root.addView(subtitle, subtitleParams);

        ScrollView toolsScroll = new ScrollView(this);
        toolsScroll.setClipToPadding(false);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(0, 0, 0, dp(12));
        toolsScroll.addView(content, new ScrollView.LayoutParams(-1, -2));
        root.addView(toolsScroll, new LinearLayout.LayoutParams(-1, 0, 1));

        toolboxStatusText = new TextView(this);
        toolboxStatusText.setText(getToolboxSummary());
        toolboxStatusText.setTextSize(17);
        toolboxStatusText.setLineSpacing(dp(3), 1.14f);
        toolboxStatusText.setTextColor(getColorRes("accent_dark"));
        toolboxStatusText.setPadding(dp(16), dp(14), dp(16), dp(14));
        toolboxStatusText.setBackgroundResource(getDrawableId("bg_toolbox_status"));
        content.addView(toolboxStatusText, new LinearLayout.LayoutParams(-1, -2));

        content.addView(createBreathingCard());
        content.addView(createMoodCard());
        content.addView(createReflectionCard());

        setContentView(root);
        animateScreenIn(root);
        animatePop(toolboxStatusText, 40);
        if (breathCircle != null) {
            animatePop(breathCircle, 80);
        }
    }

    private View createBreathingCard() {
        LinearLayout card = toolboxCard();

        TextView title = cardTitle("一分钟呼吸");
        card.addView(title);

        TextView body = cardBody("跟着圆圈慢慢吸气、停一下、再呼出去。只做一轮也算。");
        card.addView(body);

        breathCircle = new BreathCircleView(this);
        LinearLayout.LayoutParams circleParams = new LinearLayout.LayoutParams(dp(170), dp(170));
        circleParams.gravity = Gravity.CENTER_HORIZONTAL;
        circleParams.topMargin = dp(12);
        circleParams.bottomMargin = dp(12);
        card.addView(breathCircle, circleParams);

        Button toggle = primaryButton("开始呼吸");
        toggle.setOnClickListener(v -> {
            boolean nextRunning = !breathCircle.isRunning();
            breathCircle.setRunning(nextRunning);
            toggle.setText(nextRunning ? "暂停呼吸" : "开始呼吸");
        });
        card.addView(toggle, new LinearLayout.LayoutParams(-1, dp(52)));
        return card;
    }

    private View createMoodCard() {
        LinearLayout card = toolboxCard();
        card.addView(cardTitle("现在的状态"));
        card.addView(cardBody("不用打分很精确，只是给自己一个温柔的标记。"));

        selectedMood = getTodayMood();
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(-1, dp(52));
        rowParams.topMargin = dp(12);
        card.addView(row, rowParams);

        addMoodButton(row, "低落", 1, 0);
        addMoodButton(row, "还行", 2, dp(8));
        addMoodButton(row, "轻松", 3, dp(8));
        return card;
    }

    private View createReflectionCard() {
        LinearLayout card = toolboxCard();
        card.addView(cardTitle("今天的小事"));
        card.addView(cardBody("写一句已经发生的安稳小事，再给自己留一个很小的下一步。"));

        gratitudeInput = toolboxEditText("例如：今天晒到了一点太阳");
        gratitudeInput.setText(getTodayString(KEY_TOOLBOX_GRATITUDE));
        LinearLayout.LayoutParams gratitudeParams = new LinearLayout.LayoutParams(-1, dp(58));
        gratitudeParams.topMargin = dp(12);
        card.addView(gratitudeInput, gratitudeParams);

        gentleActionInput = toolboxEditText("例如：喝半杯水，或把窗帘拉开");
        gentleActionInput.setText(getTodayString(KEY_TOOLBOX_ACTION));
        LinearLayout.LayoutParams actionParams = new LinearLayout.LayoutParams(-1, dp(58));
        actionParams.topMargin = dp(10);
        card.addView(gentleActionInput, actionParams);

        Button saveButton = primaryButton("保存今天");
        saveButton.setOnClickListener(v -> saveToolboxReflection());
        LinearLayout.LayoutParams saveParams = new LinearLayout.LayoutParams(-1, dp(52));
        saveParams.topMargin = dp(12);
        card.addView(saveButton, saveParams);
        return card;
    }

    private LinearLayout toolboxCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(16), dp(18), dp(16));
        card.setBackgroundResource(getDrawableId("bg_toolbox_card"));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = dp(14);
        card.setLayoutParams(params);
        return card;
    }

    private TextView cardTitle(String text) {
        TextView title = new TextView(this);
        title.setText(text);
        title.setTextSize(21);
        title.setTextColor(getColorRes("ink"));
        title.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        return title;
    }

    private TextView cardBody(String text) {
        TextView body = new TextView(this);
        body.setText(text);
        body.setTextSize(16);
        body.setLineSpacing(dp(3), 1.12f);
        body.setTextColor(getColorRes("muted_ink"));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = dp(6);
        body.setLayoutParams(params);
        return body;
    }

    private EditText toolboxEditText(String hint) {
        EditText editText = new EditText(this);
        editText.setTextSize(16);
        editText.setSingleLine(true);
        editText.setHint(hint);
        editText.setTextColor(getColorRes("ink"));
        editText.setHintTextColor(getColorRes("muted_ink"));
        editText.setBackgroundResource(getDrawableId("bg_input_field"));
        editText.setPadding(dp(16), 0, dp(16), 0);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        return editText;
    }

    private void addMoodButton(LinearLayout row, String text, int mood, int leftMargin) {
        Button button = selectedMood == mood ? primaryButton(text) : secondaryButton(text);
        button.setOnClickListener(v -> {
            selectedMood = mood;
            saveTodayMood(mood);
            Toast.makeText(this, "已记录：" + text, Toast.LENGTH_SHORT).show();
            buildToolboxUi();
        });
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, -1, 1);
        params.leftMargin = leftMargin;
        row.addView(button, params);
    }

    private void saveToolboxReflection() {
        String gratitude = gratitudeInput == null ? "" : gratitudeInput.getText().toString().trim();
        String action = gentleActionInput == null ? "" : gentleActionInput.getText().toString().trim();
        prefs.edit()
                .putString(KEY_TOOLBOX_DATE, getTodayKey())
                .putInt(KEY_TOOLBOX_MOOD, selectedMood)
                .putString(KEY_TOOLBOX_GRATITUDE, gratitude)
                .putString(KEY_TOOLBOX_ACTION, action)
                .apply();
        if (toolboxStatusText != null) {
            toolboxStatusText.setText(getToolboxSummary());
        }
        Toast.makeText(this, "已保存今天的小记录", Toast.LENGTH_SHORT).show();
        hideToolboxKeyboard();
    }

    private void saveTodayMood(int mood) {
        prefs.edit()
                .putString(KEY_TOOLBOX_DATE, getTodayKey())
                .putInt(KEY_TOOLBOX_MOOD, mood)
                .apply();
    }

    private int getTodayMood() {
        if (!getTodayKey().equals(prefs.getString(KEY_TOOLBOX_DATE, ""))) {
            return 2;
        }
        return prefs.getInt(KEY_TOOLBOX_MOOD, 2);
    }

    private String getTodayString(String key) {
        if (!getTodayKey().equals(prefs.getString(KEY_TOOLBOX_DATE, ""))) {
            return "";
        }
        return prefs.getString(key, "");
    }

    private String getToolboxSummary() {
        String moodText = moodLabel(getTodayMood());
        String gratitude = getTodayString(KEY_TOOLBOX_GRATITUDE).trim();
        String action = getTodayString(KEY_TOOLBOX_ACTION).trim();
        if (gratitude.isEmpty() && action.isEmpty()) {
            return "今天状态：" + moodText + "\n先做一分钟呼吸，或者写一句今天还算安稳的小事。";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("今天状态：").append(moodText);
        if (!gratitude.isEmpty()) {
            builder.append("\n安稳小事：").append(gratitude);
        }
        if (!action.isEmpty()) {
            builder.append("\n下一小步：").append(action);
        }
        return builder.toString();
    }

    private String moodLabel(int mood) {
        if (mood <= 1) {
            return "有点低落";
        }
        if (mood >= 3) {
            return "稍微轻松";
        }
        return "还行，慢慢来";
    }

    private void hideToolboxKeyboard() {
        View focused = getCurrentFocus();
        if (focused == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(focused.getWindowToken(), 0);
        }
        focused.clearFocus();
    }

    private void returnToChat() {
        showingRecommendations = false;
        showingToolbox = false;
        View current = getCurrentContentView();
        current.animate()
                .alpha(0f)
                .translationY(dp(6))
                .setDuration(120)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> {
                    resetViewAnimationState(current);
                    buildUi();
                    renderMessages();
                })
                .start();
    }

    private View getCurrentContentView() {
        View content = findViewById(android.R.id.content);
        if (content instanceof ViewGroup) {
            ViewGroup contentGroup = (ViewGroup) content;
            if (contentGroup.getChildCount() > 0) {
                return contentGroup.getChildAt(0);
            }
        }
        return getWindow().getDecorView();
    }

    private void resetWindowAnimationState() {
        resetViewAnimationState(getWindow().getDecorView());
    }

    private void resetViewAnimationState(View view) {
        if (view == null) {
            return;
        }
        view.animate().cancel();
        view.setAlpha(1f);
        view.setTranslationY(0f);
        view.setScaleX(1f);
        view.setScaleY(1f);
    }

    private void installKeyboardLiftListener(View screenRoot) {
        screenRoot.getViewTreeObserver().addOnGlobalLayoutListener(() -> updateKeyboardLift(screenRoot));
        screenRoot.post(() -> updateKeyboardLift(screenRoot));
    }

    private void updateKeyboardLift(View screenRoot) {
        if (screenRoot == null || bottomInputPanel == null || input == null || showingRecommendations) {
            return;
        }
        Rect visibleFrame = new Rect();
        screenRoot.getWindowVisibleDisplayFrame(visibleFrame);

        int rootHeight = screenRoot.getRootView().getHeight();
        int keyboardHeight = rootHeight - visibleFrame.bottom;
        int visibleBottom = visibleFrame.bottom;
        boolean imeVisible = keyboardHeight > dp(140);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsets insets = screenRoot.getRootWindowInsets();
            if (insets != null) {
                int imeBottom = insets.getInsets(WindowInsets.Type.ime()).bottom;
                int navigationBottom = insets.getInsets(WindowInsets.Type.navigationBars()).bottom;
                imeVisible = insets.isVisible(WindowInsets.Type.ime()) && imeBottom > navigationBottom;
                if (imeVisible) {
                    keyboardHeight = imeBottom;
                    visibleBottom = rootHeight - keyboardHeight;
                }
            }
        }

        boolean keyboardVisible = input.hasFocus() && imeVisible;
        int lift = 0;
        if (keyboardVisible) {
            int[] panelLocation = new int[2];
            bottomInputPanel.getLocationOnScreen(panelLocation);
            int panelBottomWithoutTranslation = panelLocation[1]
                    + bottomInputPanel.getHeight()
                    - Math.round(bottomInputPanel.getTranslationY());
            lift = Math.max(0, panelBottomWithoutTranslation - visibleBottom + dp(10));
        }
        applyKeyboardLift(lift);
    }

    private void applyKeyboardLift(int liftPx) {
        if (keyboardLiftPx == liftPx) {
            return;
        }
        keyboardLiftPx = liftPx;
        if (bottomInputPanel != null) {
            bottomInputPanel.animate().cancel();
            bottomInputPanel.animate()
                    .translationY(-liftPx)
                    .setDuration(liftPx > 0 ? 180 : 140)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }
        if (scrollView != null) {
            scrollView.setPadding(
                    scrollView.getPaddingLeft(),
                    scrollView.getPaddingTop(),
                    scrollView.getPaddingRight(),
                    liftPx > 0 ? liftPx + dp(12) : 0);
            if (liftPx > 0 && chatList != null) {
                scrollView.post(() -> scrollView.smoothScrollTo(0, chatList.getBottom()));
            }
        }
    }

    private void showSoftKeyboard() {
        if (input == null) {
            return;
        }
        input.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(input, InputMethodManager.SHOW_FORCED);
        }
    }

    private void showKeyDialog() {
        EditText keyInput = new EditText(this);
        keyInput.setHint("sk-...");
        keyInput.setSingleLine(true);
        keyInput.setText(prefs.getString(KEY_API, ""));
        keyInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        keyInput.setPadding(dp(12), dp(8), dp(12), dp(8));

        new AlertDialog.Builder(this)
                .setTitle("MiniMax API 密钥")
                .setMessage("密钥只保存在这台手机本地。建议单独创建一个可随时撤销的 key。")
                .setView(keyInput)
                .setPositiveButton("保存", (dialog, which) -> {
                    prefs.edit().putString(KEY_API, keyInput.getText().toString().trim()).apply();
                    Toast.makeText(this, "已保存", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showMemoryDialog() {
        List<String> items = getMemoryItems();
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(items.isEmpty() ? "已记住的信息" : "点一条记忆可删除")
                .setPositiveButton("关闭", null);
        if (items.isEmpty()) {
            builder.setMessage("现在还没有记录到长期信息。多聊几次后，我会自动整理你明确提到的人名、家庭关系和重要事件。");
        } else {
            builder.setItems(items.toArray(new String[0]), (dialog, which) -> showDeleteMemoryItemDialog(items, which));
            builder.setNegativeButton("清空全部", (dialog, which) -> clearMemoryNotes());
        }
        builder.show();
    }

    private void showDeleteMemoryItemDialog(List<String> items, int index) {
        if (index < 0 || index >= items.size()) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("删除这条记忆？")
                .setMessage(items.get(index))
                .setPositiveButton("删除", (dialog, which) -> deleteMemoryItem(items, index))
                .setNegativeButton("取消", (dialog, which) -> showMemoryDialog())
                .show();
    }

    private void deleteMemoryItem(List<String> items, int index) {
        if (index < 0 || index >= items.size()) {
            return;
        }
        items.remove(index);
        saveMemoryItems(items);
        Toast.makeText(this, "已删除这条记忆", Toast.LENGTH_SHORT).show();
        showMemoryDialog();
    }

    private void clearMemoryNotes() {
        prefs.edit()
                .remove(KEY_MEMORY_NOTES)
                .remove(KEY_MEMORY_USER_COUNT)
                .apply();
        Toast.makeText(this, "记忆已清空", Toast.LENGTH_SHORT).show();
    }

    private String getRecommendationWelcomeText() {
        return "选一个栏目看看。\n\n今日新闻：轻一点的简报。\n开心一下：几句有趣的话。\n心灵鸡汤：名言和小故事。";
    }

    private void loadRecommendation(String type) {
        if (recommendationOutput == null) {
            return;
        }
        String cached = getCachedRecommendation(type);
        if (!"news".equals(type)) {
            if (!cached.isEmpty()) {
                showRecommendationText(cached);
                return;
            }
            String fallback = getFallbackRecommendation(type);
            cacheRecommendation(type, fallback);
            showRecommendationText(fallback);
            return;
        }

        setRecommendationLoading(true);
        recommendationOutput.setText("");
        executor.execute(() -> {
            try {
                String text = requestLatestNews();
                if (text.isEmpty()) {
                    text = cached.isEmpty() ? getFallbackRecommendation(type) : cached;
                }
                cacheRecommendation(type, text);
                String finalText = text;
                runOnUiThread(() -> {
                    if (showingRecommendations && recommendationOutput != null) {
                        showRecommendationText(finalText);
                        setRecommendationLoading(false);
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    if (showingRecommendations && recommendationOutput != null) {
                        String fallback = cached.isEmpty() ? getFallbackRecommendation(type) : cached;
                        showRecommendationText(fallback);
                        setRecommendationLoading(false);
                    }
                });
            }
        });
    }

    private String requestRecommendation(String apiKey, String type) throws Exception {
        JSONObject payload = new JSONObject();
        payload.put("model", DEFAULT_MODEL);
        payload.put("temperature", "news".equals(type) ? 0.2 : 0.85);
        payload.put("max_tokens", 360);

        JSONArray apiMessages = new JSONArray();
        JSONObject system = new JSONObject();
        system.put("role", "system");
        system.put("content", recommendationSystemPrompt(type));
        apiMessages.put(system);

        JSONObject user = new JSONObject();
        user.put("role", "user");
        user.put("content", recommendationUserPrompt(type));
        apiMessages.put(user);
        payload.put("messages", apiMessages);

        String response = postJson(apiKey, payload.toString(), 5000, 9000);
        JSONObject json = new JSONObject(response);
        return json.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .optString("content", "");
    }

    private String requestLatestNews() throws Exception {
        URL url = new URL(NEWS_RSS_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(8000);
        conn.setRequestProperty("User-Agent", "GrandmaCompanion/1.0");
        String xml = readAll(conn.getInputStream());
        List<String> titles = parseRssTitles(xml, 4);
        if (titles.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("今日新闻：\n\n");
        for (int i = 0; i < titles.size(); i++) {
            builder.append(i + 1).append(". ").append(titles.get(i)).append("\n\n");
        }
        builder.append("慢慢看就好，知道几件正在发生的事就够了。");
        return builder.toString().trim();
    }

    private List<String> parseRssTitles(String xml, int limit) {
        List<String> titles = new ArrayList<>();
        Pattern itemPattern = Pattern.compile("(?s)<item\\b.*?</item>");
        Matcher itemMatcher = itemPattern.matcher(xml);
        Pattern titlePattern = Pattern.compile("(?s)<title>(.*?)</title>");
        while (itemMatcher.find() && titles.size() < limit) {
            Matcher titleMatcher = titlePattern.matcher(itemMatcher.group());
            if (titleMatcher.find()) {
                String title = cleanXmlText(titleMatcher.group(1));
                if (!title.isEmpty()) {
                    titles.add(title);
                }
            }
        }
        return titles;
    }

    private String cleanXmlText(String text) {
        return text
                .replace("<![CDATA[", "")
                .replace("]]>", "")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String recommendationSystemPrompt(String type) {
        if ("news".equals(type)) {
            return "你为一个中文陪伴应用写今日新闻简报。语气平和，不制造焦虑。"
                    + "如果你不能确认实时新闻，不要编造具体事件、时间、数字和来源；可以改写成今日值得关注的公共议题。"
                    + "输出 3 条，每条 1 到 2 句，最后加一句轻松收尾。";
        }
        if ("fun".equals(type)) {
            return "你为一个中文陪伴应用写“开心一下”，读者主要是中老年人。"
                    + "内容要温和、生活化、容易懂，像菜市场、邻里、养花、做饭、老朋友聊天里的小趣事。"
                    + "避免网络梗、年轻人自嘲、抽象冷笑话、夸张段子和需要互联网语境才能懂的幽默。"
                    + "不要冒犯，不要低俗，不要拿疾病、年龄、苦难开玩笑。输出 3 到 5 条，每条 1 到 2 句。";
        }
        return "你为一个中文陪伴应用写“心灵鸡汤”，读者主要是中老年人。"
                + "不要只写短句，要写 2 个国内外励志人物故事。每个故事先写人物名字，再写一句广为人知的名言或意译警句，下面用 3 到 5 句介绍简短事迹。"
                + "人物可以来自中国和世界，例如苏轼、司马迁、林肯、海伦·凯勒、曼德拉、居里夫人、屠呦呦等。"
                + "语气温和、通透、朴素，不要鸡血，不要说教，不要伪造具体年份和精确出处。";
    }

    private String recommendationUserPrompt(String type) {
        String today = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA).format(new Date());
        if ("news".equals(type)) {
            return "今天是" + today + "。请给我一份适合普通人轻松浏览的今日新闻简报。";
        }
        if ("fun".equals(type)) {
            return "请给我几条适合中老年人会心一笑的生活小趣话，亲切、朴素、不要网络梗。";
        }
        return "请给我 2 个适合今天阅读的励志人物故事。格式为：人物名、名言、简短事迹、给今天的一点启发。";
    }

    private String getFallbackRecommendation(String type) {
        int dayOfYear = Calendar.getInstance(Locale.CHINA).get(Calendar.DAY_OF_YEAR);
        if ("fun".equals(type)) {
            return FUN_FALLBACKS[dayOfYear % FUN_FALLBACKS.length];
        }
        if ("wisdom".equals(type)) {
            return WISDOM_FALLBACKS[dayOfYear % WISDOM_FALLBACKS.length];
        }
        return "今日新闻暂时没有取到。\n\n可以先看三个轻量方向：天气变化、身边社区信息、最近的文化或科技小新闻。真正重要的事不用急着一次看完。";
    }

    private String getCachedRecommendation(String type) {
        String today = getTodayKey();
        if (!today.equals(prefs.getString(KEY_RECOMMENDATION_DATE + type, ""))) {
            return "";
        }
        String cached = prefs.getString(KEY_RECOMMENDATION_TEXT + type, "").trim();
        if ("wisdom".equals(type) && isOutdatedWisdomRecommendation(cached)) {
            prefs.edit()
                    .remove(KEY_RECOMMENDATION_DATE + type)
                    .remove(KEY_RECOMMENDATION_TEXT + type)
                    .apply();
            return "";
        }
        return cached;
    }

    private boolean isOutdatedWisdomRecommendation(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        String trimmed = text.trim();
        return trimmed.startsWith("今天这句")
                || (!trimmed.contains("心灵鸡汤") && !trimmed.contains("事迹") && trimmed.length() < 160);
    }

    private void cacheRecommendation(String type, String text) {
        if (text == null || text.trim().isEmpty()) {
            return;
        }
        prefs.edit()
                .putString(KEY_RECOMMENDATION_DATE + type, getTodayKey())
                .putString(KEY_RECOMMENDATION_TEXT + type, text.trim())
                .apply();
    }

    private String getTodayKey() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(new Date());
    }

    private void migrateRecommendationStyleIfNeeded() {
        int styleVersion = prefs.getInt(KEY_RECOMMENDATION_STYLE_VERSION, 0);
        if (styleVersion >= RECOMMENDATION_STYLE_VERSION) {
            return;
        }
        SharedPreferences.Editor editor = prefs.edit()
                .putInt(KEY_RECOMMENDATION_STYLE_VERSION, RECOMMENDATION_STYLE_VERSION);
        String[] types = {"news", "fun", "wisdom"};
        for (String type : types) {
            editor.remove(KEY_RECOMMENDATION_DATE + type);
            editor.remove(KEY_RECOMMENDATION_TEXT + type);
        }
        editor.apply();
    }

    private void showRecommendationText(String text) {
        recommendationOutput.setText(text);
        animateRecommendationOutput();
    }

    private void animateRecommendationOutput() {
        if (recommendationOutput == null) {
            return;
        }
        AlphaAnimation fade = new AlphaAnimation(0f, 1f);
        TranslateAnimation slide = new TranslateAnimation(0, 0, dp(20), 0);
        AnimationSet set = new AnimationSet(true);
        set.addAnimation(fade);
        set.addAnimation(slide);
        set.setDuration(280);
        recommendationOutput.startAnimation(set);
    }

    private void setRecommendationLoading(boolean loading) {
        if (recommendationNewsButton != null) {
            recommendationNewsButton.setEnabled(!loading);
        }
        if (recommendationFunButton != null) {
            recommendationFunButton.setEnabled(!loading);
        }
        if (recommendationWisdomButton != null) {
            recommendationWisdomButton.setEnabled(!loading);
        }
        if (recommendationProgress != null) {
            recommendationProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    private void sendCurrentText() {
        String text = input.getText().toString().trim();
        if (text.isEmpty()) {
            return;
        }
        hideKeyboard();
        input.setText("");
        addUserMessage(text);

        if (looksLikeCrisis(text)) {
            addAssistantMessage("我听见你现在很难受。先别一个人扛着，请马上让身边的人过来陪你，或拨打当地急救电话。你可以直接说：我现在不安全，需要你马上陪我。");
            return;
        }

        String apiKey = prefs.getString(KEY_API, "").trim();
        if (apiKey.isEmpty()) {
            addAssistantMessage("还没有设置 MiniMax API 密钥。请点上方“设置密钥”，保存后我就能开始陪你聊天。");
            return;
        }
        setLoading(true);
        callMiniMax(apiKey);
    }

    private boolean looksLikeCrisis(String text) {
        String compact = text.replace(" ", "");
        String[] risky = {"不想活", "想死", "自杀", "轻生", "活不下去", "结束生命", "我不安全"};
        for (String word : risky) {
            if (compact.contains(word)) {
                return true;
            }
        }
        return false;
    }

    private void callMiniMax(String apiKey) {
        List<ChatMessage> snapshot = new ArrayList<>(messages);
        executor.execute(() -> {
            try {
                JSONObject payload = new JSONObject();
                payload.put("model", DEFAULT_MODEL);
                payload.put("temperature", 0.6);
                payload.put("max_tokens", 700);

                JSONArray apiMessages = new JSONArray();
                JSONObject system = new JSONObject();
                system.put("role", "system");
                system.put("content", systemPrompt());
                apiMessages.put(system);

                String memoryNotes = getMemoryNotes();
                if (!memoryNotes.isEmpty()) {
                    JSONObject memory = new JSONObject();
                    memory.put("role", "system");
                    memory.put("content", "下面是用户明确分享过、需要长期记住的背景信息。只在相关时自然使用，不要突兀复述：\n" + memoryNotes);
                    apiMessages.put(memory);
                }

                int start = Math.max(0, snapshot.size() - MAX_HISTORY_MESSAGES);
                for (int i = start; i < snapshot.size(); i++) {
                    ChatMessage message = snapshot.get(i);
                    JSONObject item = new JSONObject();
                    item.put("role", message.role);
                    item.put("content", sanitizeMessageForPrompt(message.content));
                    apiMessages.put(item);
                }
                payload.put("messages", apiMessages);

                String response = postJson(apiKey, payload.toString());
                JSONObject json = new JSONObject(response);
                String content = json.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .optString("content", "");
                content = cleanAssistantText(content);
                if (content.isEmpty()) {
                    content = "我在。你刚刚说的我收到了，我们可以慢慢聊，不用急着得出结论。";
                }
                String finalContent = content;
                runOnUiThread(() -> {
                    addAssistantMessage(finalContent);
                    setLoading(false);
                });
                maybeUpdateMemory(apiKey, snapshot, finalContent);
            } catch (Exception e) {
                runOnUiThread(() -> {
                    addAssistantMessage("现在连接 MiniMax 没成功。请检查网络、MiniMax 密钥和账户额度。错误：" + formatConnectionError(e));
                    setLoading(false);
                });
            }
        });
    }

    private String postJson(String apiKey, String body) throws Exception {
        return postJson(apiKey, body, 15000, 30000);
    }

    private String postJson(String apiKey, String body, int connectTimeoutMs, int readTimeoutMs) throws Exception {
        Exception lastError = null;
        for (String apiUrl : API_URLS) {
            try {
                return postJsonToUrl(apiUrl, apiKey, body, connectTimeoutMs, readTimeoutMs);
            } catch (Exception e) {
                lastError = e;
            }
        }
        throw lastError == null ? new IllegalStateException("MiniMax request failed") : lastError;
    }

    private String postJsonToUrl(String apiUrl, String apiKey, String body, int connectTimeoutMs, int readTimeoutMs) throws Exception {
        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(connectTimeoutMs);
        conn.setReadTimeout(readTimeoutMs);
        conn.setDoOutput(true);
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }
        int code = conn.getResponseCode();
        InputStream stream = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();
        String text = readAll(stream);
        if (code < 200 || code >= 300) {
            throw new IllegalStateException("HTTP " + code + " " + text);
        }
        return text;
    }

    private String readAll(InputStream stream) throws Exception {
        if (stream == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }

    private String formatConnectionError(Exception e) {
        String message = e.getMessage();
        if (message == null || message.trim().isEmpty()) {
            return "没有返回具体错误";
        }
        String lower = message.toLowerCase(Locale.ROOT);
        if (message.contains("401") || lower.contains("unauthorized")) {
            return "密钥无效或类型不匹配，请重新设置 MiniMax API key";
        }
        if (message.contains("403")) {
            return "账户没有权限或额度不可用";
        }
        if (message.contains("429")) {
            return "请求太频繁或额度已用完";
        }
        if (lower.contains("unable to resolve host") || lower.contains("timed out")) {
            return "网络无法访问 MiniMax 服务器";
        }
        return message;
    }

    private void maybeUpdateMemory(String apiKey, List<ChatMessage> snapshot, String latestAssistantText) {
        int userMessageCount = countUserMessages(snapshot);
        int lastMemoryUserCount = prefs.getInt(KEY_MEMORY_USER_COUNT, 0);
        if (userMessageCount < lastMemoryUserCount + MEMORY_UPDATE_EVERY_USER_MESSAGES) {
            return;
        }

        try {
            JSONObject payload = new JSONObject();
            payload.put("model", DEFAULT_MODEL);
            payload.put("temperature", 0.1);
            payload.put("max_tokens", 500);

            JSONArray apiMessages = new JSONArray();
            JSONObject system = new JSONObject();
            system.put("role", "system");
            system.put("content", memorySystemPrompt());
            apiMessages.put(system);

            JSONObject user = new JSONObject();
            user.put("role", "user");
            user.put("content", buildMemoryUpdatePrompt(snapshot, latestAssistantText));
            apiMessages.put(user);
            payload.put("messages", apiMessages);

            String response = postJson(apiKey, payload.toString());
            JSONObject json = new JSONObject(response);
            String memory = json.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .optString("content", "");
            memory = cleanMemoryText(memory);
            SharedPreferences.Editor editor = prefs.edit()
                    .putInt(KEY_MEMORY_USER_COUNT, userMessageCount);
            if (!memory.isEmpty()) {
                editor.putString(KEY_MEMORY_NOTES, memory);
            }
            editor.apply();
        } catch (Exception ignored) {
        }
    }

    private int countUserMessages(List<ChatMessage> source) {
        int count = 0;
        for (ChatMessage message : source) {
            if ("user".equals(message.role)) {
                count++;
            }
        }
        return count;
    }

    private String memorySystemPrompt() {
        return "你负责维护一个陪伴聊天应用的长期记忆。"
                + "只记录用户明确说过的稳定事实，例如家庭关系、人名、重要事件、兴趣、习惯、长期偏好。"
                + "不要记录你自己的建议，不要编造，不要推断诊断，不要把一时情绪写成长期标签。"
                + "把旧记忆和新对话合并去重，最多保留 12 条。"
                + "只输出中文短条目列表，每条以“- ”开头；如果没有值得保留的信息，输出原有记忆。";
    }

    private String buildMemoryUpdatePrompt(List<ChatMessage> snapshot, String latestAssistantText) {
        StringBuilder builder = new StringBuilder();
        String currentMemory = getMemoryNotes();
        builder.append("现有记忆：\n");
        builder.append(currentMemory.isEmpty() ? "无" : currentMemory);
        builder.append("\n\n最近对话：\n");

        int start = Math.max(0, snapshot.size() - MAX_HISTORY_MESSAGES);
        for (int i = start; i < snapshot.size(); i++) {
            ChatMessage message = snapshot.get(i);
            builder.append("user".equals(message.role) ? "用户：" : "安心伴：");
            builder.append(sanitizeMessageForPrompt(message.content));
            builder.append("\n");
        }
        if (latestAssistantText != null && !latestAssistantText.trim().isEmpty()) {
            builder.append("安心伴：");
            builder.append(sanitizeMessageForPrompt(latestAssistantText));
            builder.append("\n");
        }
        return builder.toString();
    }

    private String cleanMemoryText(String text) {
        String cleaned = cleanAssistantText(text);
        cleaned = cleaned.replace("```", "").trim();
        if (cleaned.equals("无") || cleaned.equals("没有") || cleaned.equals("暂无")) {
            return getMemoryNotes();
        }
        if (cleaned.length() > 1200) {
            cleaned = cleaned.substring(0, 1200).trim();
        }
        return cleaned;
    }

    private String systemPrompt() {
        return "你是一位像同龄朋友一样聊天的中文陪伴者。"
                + "称呼用户为“你”，不要使用亲属称呼、年龄称呼，也不要用居高临下的照顾口吻。"
                + "目标是自然陪伴、情绪理解、轻轻接住对方的话，而不是每次给方案。"
                + "请使用简短、亲切、口语化中文，每次回复 2 到 5 句，像朋友聊天，避免说教、口号和心理咨询模板。"
                + "不要每次都给身体照料建议或固定小行动；只有当上下文真的适合时才偶尔提一个。"
                + "不要反复总结“我听见你很难受”这类模板，要顺着用户具体说的话回应。"
                + "当她觉得自己没用、不够好时，先承认这种感受很折磨，再用具体、平实的话陪她看见一点可松动的地方。"
                + "不要诊断，不要承诺治愈，不要让她独自处理严重风险。"
                + "如果她表达自伤、自杀、活不下去、伤害自己，请明确建议马上联系身边的人、医生或当地急救电话。";
    }

    private String cleanModelText(String text) {
        return text.replaceAll("(?s)<think>.*?</think>", "").trim();
    }

    private String cleanAssistantText(String text) {
        String cleaned = cleanModelText(text);
        cleaned = cleaned.replace("姥姥，", "");
        cleaned = cleaned.replace("姥姥", "你");
        cleaned = cleaned.replace("奶奶，", "");
        cleaned = cleaned.replace("奶奶", "你");
        cleaned = cleaned.replace("老人家，", "");
        cleaned = cleaned.replace("老人家", "你");
        cleaned = cleaned.replace("您", "你");
        return cleaned.trim();
    }

    private String sanitizeMessageForPrompt(String text) {
        return cleanAssistantText(text);
    }

    private String getMemoryNotes() {
        return sanitizeMessageForPrompt(prefs.getString(KEY_MEMORY_NOTES, "").trim());
    }

    private List<String> getMemoryItems() {
        List<String> items = new ArrayList<>();
        String notes = getMemoryNotes();
        if (notes.isEmpty()) {
            return items;
        }
        String[] lines = notes.split("\\r?\\n");
        for (String line : lines) {
            String item = line.trim()
                    .replaceFirst("^[-•*]\\s*", "")
                    .trim();
            if (!item.isEmpty()) {
                items.add(item);
            }
        }
        return items;
    }

    private void saveMemoryItems(List<String> items) {
        if (items.isEmpty()) {
            prefs.edit().remove(KEY_MEMORY_NOTES).apply();
            return;
        }
        StringBuilder builder = new StringBuilder();
        for (String item : items) {
            String cleaned = sanitizeMessageForPrompt(item).trim();
            if (!cleaned.isEmpty()) {
                if (builder.length() > 0) {
                    builder.append("\n");
                }
                builder.append("- ").append(cleaned.replaceFirst("^[-•*]\\s*", "").trim());
            }
        }
        prefs.edit().putString(KEY_MEMORY_NOTES, builder.toString()).apply();
    }

    private void addUserMessage(String text) {
        messages.add(new ChatMessage("user", text));
        trimStoredMessages();
        saveMessages();
        addBubble("你", text, true, true);
    }

    private void addAssistantMessage(String text) {
        messages.add(new ChatMessage("assistant", text));
        trimStoredMessages();
        saveMessages();
        lastAssistantText = text;
        addBubble("安心伴", text, false, true);
    }

    private void replaceLastAssistantMessage(String text) {
        for (int i = messages.size() - 1; i >= 0; i--) {
            if ("assistant".equals(messages.get(i).role)) {
                messages.set(i, new ChatMessage("assistant", text));
                break;
            }
        }
        lastAssistantText = text;
        saveMessages();
        renderMessages();
    }

    private void renderMessages() {
        chatList.removeAllViews();
        for (ChatMessage message : messages) {
            addBubble("user".equals(message.role) ? "你" : "安心伴", message.content, "user".equals(message.role), false);
        }
    }

    private void addBubble(String name, String text, boolean isUser, boolean animate) {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setGravity(isUser ? Gravity.RIGHT : Gravity.LEFT);
        wrap.setPadding(0, dp(8), 0, dp(8));

        TextView meta = new TextView(this);
        meta.setText(name + " · " + new SimpleDateFormat("HH:mm", Locale.CHINA).format(new Date()));
        meta.setTextSize(13);
        meta.setTextColor(getColorRes("muted_ink"));
        meta.setGravity(isUser ? Gravity.RIGHT : Gravity.LEFT);
        wrap.addView(meta, new LinearLayout.LayoutParams(-1, dp(22)));

        TextView bubble = new TextView(this);
        bubble.setText(text);
        bubble.setTextSize(20);
        bubble.setLineSpacing(dp(3), 1.18f);
        bubble.setTextColor(getColorRes("ink"));
        bubble.setBackgroundResource(isUser ? getDrawableId("bg_bubble_user") : getDrawableId("bg_bubble_ai"));
        int maxWidth = getResources().getDisplayMetrics().widthPixels - dp(72);
        bubble.setMaxWidth(maxWidth);
        wrap.addView(bubble);

        chatList.addView(wrap, new LinearLayout.LayoutParams(-1, -2));
        if (animate) {
            animateBubble(wrap, isUser);
        }
        scrollView.post(() -> scrollView.smoothScrollTo(0, chatList.getBottom()));
    }

    private void setLoading(boolean loading) {
        sendButton.setEnabled(!loading);
        sendButton.setAlpha(loading ? 0.62f : 1f);
        if (chatLoadingPill != null) {
            chatLoadingPill.animate().cancel();
            chatLoadingPill.setVisibility(View.VISIBLE);
            chatLoadingPill.animate()
                    .alpha(loading ? 1f : 0f)
                    .setDuration(loading ? 180 : 220)
                    .setInterpolator(new DecelerateInterpolator())
                    .withEndAction(() -> {
                        if (!loading && chatLoadingPill != null) {
                            chatLoadingPill.setVisibility(View.INVISIBLE);
                        }
                    })
                    .start();
        }
        if (chatProgressText != null) {
            chatProgressText.clearAnimation();
        }
        sendButton.setText("发送");
    }

    private void loadMessages() {
        String raw = prefs.getString(KEY_MESSAGES, "[]");
        try {
            JSONArray array = new JSONArray(raw);
            messages.clear();
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                String role = item.optString("role", "");
                String content = item.optString("content", "");
                if (!role.isEmpty() && !content.isEmpty()) {
                    content = sanitizeMessageForPrompt(content);
                    messages.add(new ChatMessage(role, content));
                    if ("assistant".equals(role)) {
                        lastAssistantText = content;
                    }
                }
            }
        } catch (Exception ignored) {
            messages.clear();
        }
    }

    private void migratePromptStyleIfNeeded() {
        int styleVersion = prefs.getInt(KEY_PROMPT_STYLE_VERSION, 0);
        if (styleVersion >= PROMPT_STYLE_VERSION) {
            return;
        }
        prefs.edit()
                .remove(KEY_MESSAGES)
                .putInt(KEY_PROMPT_STYLE_VERSION, PROMPT_STYLE_VERSION)
                .apply();
        messages.clear();
        lastAssistantText = "";
    }

    private void saveMessages() {
        try {
            JSONArray array = new JSONArray();
            for (ChatMessage message : messages) {
                JSONObject item = new JSONObject();
                item.put("role", message.role);
                item.put("content", message.content);
                array.put(item);
            }
            prefs.edit().putString(KEY_MESSAGES, array.toString()).apply();
        } catch (Exception ignored) {
        }
    }

    private void trimStoredMessages() {
        while (messages.size() > MAX_STORED_MESSAGES) {
            messages.remove(0);
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private int getStatusBarHeight() {
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return getResources().getDimensionPixelSize(resourceId);
        }
        return dp(24);
    }

    private int getNavigationBarHeight() {
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    private int getColorRes(String name) {
        int id = getResources().getIdentifier(name, "color", getPackageName());
        return getResources().getColor(id);
    }

    private int getDrawableId(String name) {
        return getResources().getIdentifier(name, "drawable", getPackageName());
    }

    @Override
    public void onBackPressed() {
        if (showingRecommendations) {
            returnToChat();
            return;
        }
        if (showingToolbox) {
            returnToChat();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }

    private static class ChatMessage {
        final String role;
        final String content;

        ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    private class BouncingDotsView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final int[] colors = {
                getColorRes("accent"),
                getColorRes("peach"),
                getColorRes("sunny")
        };

        BouncingDotsView(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float centerY = getHeight() / 2f;
            float spacing = dp(18);
            float startX = getWidth() / 2f - spacing;
            long now = System.currentTimeMillis();
            for (int i = 0; i < 3; i++) {
                float phase = (now % 1750L) / 1750f * (float) Math.PI * 2f + i * 0.75f;
                float y = centerY - (float) Math.sin(phase) * dp(3);
                paint.setColor(colors[i]);
                paint.setAlpha(165 + i * 12);
                canvas.drawCircle(startX + i * spacing, y, dp(5), paint);
            }
            if (getVisibility() == View.VISIBLE) {
                postInvalidateDelayed(48);
            }
        }
    }

    private class BreathCircleView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private boolean running = false;
        private long startedAt = 0L;

        BreathCircleView(Context context) {
            super(context);
        }

        boolean isRunning() {
            return running;
        }

        void setRunning(boolean running) {
            this.running = running;
            startedAt = System.currentTimeMillis();
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float width = getWidth();
            float height = getHeight();
            float centerX = width / 2f;
            float centerY = height / 2f;
            long now = System.currentTimeMillis();
            float cycle = running ? ((now - startedAt) % 8000L) / 8000f : 0.72f;
            float scale;
            String label;
            if (!running) {
                scale = 0.72f;
                label = "准备好";
            } else if (cycle < 0.38f) {
                scale = 0.58f + (cycle / 0.38f) * 0.28f;
                label = "慢慢吸气";
            } else if (cycle < 0.5f) {
                scale = 0.86f;
                label = "停一下";
            } else {
                scale = 0.86f - ((cycle - 0.5f) / 0.5f) * 0.28f;
                label = "轻轻呼气";
            }

            float outer = Math.min(width, height) * 0.45f;
            float radius = outer * scale;

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(getColorRes("accent_soft"));
            paint.setAlpha(220);
            canvas.drawCircle(centerX, centerY, outer, paint);

            paint.setColor(getColorRes("peach_soft"));
            paint.setAlpha(230);
            canvas.drawCircle(centerX, centerY, radius + dp(12), paint);

            paint.setColor(getColorRes("accent_mid"));
            paint.setAlpha(185);
            canvas.drawCircle(centerX, centerY, radius, paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(getColorRes("accent_dark"));
            paint.setAlpha(120);
            paint.setStrokeWidth(dp(1.5f));
            canvas.drawCircle(centerX, centerY, radius, paint);

            paint.setStyle(Paint.Style.FILL);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
            paint.setTextSize(dp(17));
            paint.setColor(Color.WHITE);
            paint.setAlpha(245);
            canvas.drawText(label, centerX, centerY + dp(6), paint);

            if (running) {
                postInvalidateDelayed(48);
            }
        }
    }

    private class FloatingMascotView extends View {
        private final boolean rightSide;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        FloatingMascotView(Context context, boolean rightSide) {
            super(context);
            this.rightSide = rightSide;
            setAlpha(0.62f);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            long now = System.currentTimeMillis();
            float width = getWidth();
            float[] baseY = {rightSide ? getHeight() - dp(176) : dp(112)};
            int[] bodyColors = {
                    rightSide ? getColorRes("accent_soft") : getColorRes("peach_soft")
            };
            for (int i = 0; i < baseY.length; i++) {
                float wave = (float) Math.sin((now / 4800f) + i * 1.2f) * dp(2);
                float x = rightSide ? width - dp(17) : dp(17);
                float y = baseY[i] + wave;
                drawLittleFace(canvas, x, y, bodyColors[i], i);
            }
            drawSparkle(canvas, rightSide ? dp(9) : width - dp(9), getHeight() * 0.25f, now);
            drawSparkle(canvas, rightSide ? dp(14) : width - dp(14), getHeight() * 0.68f, now + 400);
            postInvalidateDelayed(120);
        }

        private void drawLittleFace(Canvas canvas, float x, float y, int color, int index) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(color);
            paint.setAlpha(230);
            float radius = dp(13);
            canvas.drawCircle(x - dp(8), y - dp(10), dp(5), paint);
            canvas.drawCircle(x + dp(8), y - dp(10), dp(5), paint);
            canvas.drawCircle(x, y, radius, paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(getColorRes("line_soft"));
            paint.setAlpha(150);
            paint.setStrokeWidth(dp(1f));
            canvas.drawCircle(x, y, radius, paint);

            paint.setColor(getColorRes("ink"));
            paint.setAlpha(180);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(x - dp(4), y - dp(2), dp(1.6f), paint);
            canvas.drawCircle(x + dp(4), y - dp(2), dp(1.6f), paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(1.3f));
            canvas.drawArc(x - dp(5), y - dp(2), x + dp(5), y + dp(7), 15, 150, false, paint);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(getColorRes("peach"));
            paint.setAlpha(110);
            canvas.drawCircle(x - dp(8), y + dp(3), dp(2.1f), paint);
            canvas.drawCircle(x + dp(8), y + dp(3), dp(2.1f), paint);
        }

        private void drawSparkle(Canvas canvas, float x, float y, long now) {
            float scale = 0.86f + 0.14f * (float) Math.sin(now / 2200f);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(getColorRes("peach"));
            paint.setAlpha(90);
            canvas.drawCircle(x, y, dp(3) * scale, paint);
            paint.setColor(getColorRes("sunny"));
            canvas.drawCircle(x + dp(8), y + dp(10), dp(2) * scale, paint);
        }
    }
}
