package edu.hebut.retrofittest.UI.fragment;


import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import edu.hebut.retrofittest.Adapter.MyMessagesListAdapter;
import edu.hebut.retrofittest.R;
import edu.hebut.retrofittest.Util.EnCodeUtils;
import edu.hebut.retrofittest.Util.SharedDataUtils;
import edu.hebut.retrofittest.chat.client.RetrofitClient;
import edu.hebut.retrofittest.chat.model.Message;
import edu.hebut.retrofittest.chat.model.User;
import edu.hebut.retrofittest.chat.service.ChatApi;
import io.noties.markwon.Markwon;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DecideFragment extends Fragment implements MessagesListAdapter.OnLoadMoreListener {

    private User mUser;
    private User mAiAssistant;

    private ChatApi chatApi;
    MyMessagesListAdapter mMessagesListAdapter;
    private MessagesList messagesList;
    private MessageInput input;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUser = new User("0", "User", null, true);


        String AI_ASSISTANT_AVATAR = "data:image/png;base64," +  EnCodeUtils.encodeImage(BitmapFactory.decodeResource(getResources(), R.mipmap.label));

        mAiAssistant = new User("1", "AiAssistant", AI_ASSISTANT_AVATAR, true);

        chatApi = RetrofitClient.getInstance().create(ChatApi.class);

        MessagesListAdapter.HoldersConfig holdersConfig = new MessagesListAdapter.HoldersConfig();

        holdersConfig.setIncomingTextLayout(R.layout.item_custom_incoming_text_message);

        /*
         * senderId:自己的id，用于区分自己和对方，控制消息气泡的位置。
         * imageLoader:图像加载器
         *
         * */
        mMessagesListAdapter = new MyMessagesListAdapter("0",
                /*holdersConfig,*/
                (imageView, url, payload)
                        -> Glide.with(getContext()).load(url).into(imageView));

        //滑倒顶部时加载历史记录
        mMessagesListAdapter.setLoadMoreListener(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // 实例fragment_decision布局
        View fgDecide = inflater.inflate(R.layout.fragment_decide, container, false);

        // 获取消息列表和输入框
        messagesList = fgDecide.findViewById(R.id.messagesList);
        input = fgDecide.findViewById(R.id.input);

        //发送输入框中的文本，addToStart的第二个参数是列表滚动到底部
        input.setInputListener(createInputListener());

        //小加号按钮点击事件
        input.setAttachmentsListener(() -> {
            String aiMsgId = "ai_" + System.currentTimeMillis();
            // 创建占位消息并获取引用
            Message placeholderMsg = new Message(
                    aiMsgId,
                    mAiAssistant,
                    "思考中\n");

            // TODO: 处理附件点击事件
            mMessagesListAdapter.addToStart(placeholderMsg, true);

        });

        // 设置消息列表适配器
        messagesList.setAdapter(mMessagesListAdapter);

        //初始化时调用一次加载历史记录
        onLoadMore(0, 1);

        return fgDecide;
    }

    @Override
    public void onLoadMore(int page, int totalItemsCount) {
        new Handler().postDelayed(() -> {
            // TODO: 处理加载更多消息的逻辑
        }, 1000);
    }

    // 修改后的消息发送逻辑（带状态控制）
    private void sendMessageToServer(String message) {
        // 禁用输入控件
        input.setInputListener(null);
        input.setEnabled(false);

        String aiMsgId = "ai_" + System.currentTimeMillis();
        // 创建占位消息并获取引用
        Message placeholderMsg = new Message(
                aiMsgId,
                mAiAssistant,
                "思考中");

        mMessagesListAdapter.addToStart(placeholderMsg, true);

        Map<String, String> params = new HashMap<>();

        params.put("query", message);
        params.put("user_id", SharedDataUtils.getLoginUser().getId().toString());

        chatApi.sendMessageStream(params).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new Thread(() -> {
                        try (BufferedReader reader = new BufferedReader(
                                new InputStreamReader(response.body().source().inputStream(), StandardCharsets.UTF_8))) {

                            StringBuilder content = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                for (char ch : line.toCharArray()) {
                                    content.append(ch);
                                    updateMessageContent(aiMsgId, content.toString());
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                    }
                                }
                            }

                            // 最终处理
                            completeMessage(aiMsgId, content.toString());
                        } catch (IOException e) {
                            handleError(aiMsgId);
                        }
                    }).start();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                handleError(aiMsgId);
            }
        });
    }

    // 消息更新方法
    private void updateMessageContent(String msgId, String newContent) {
        requireActivity().runOnUiThread(() -> {
            Message currentMessage = new Message(
                    msgId,
                    mAiAssistant,
                    newContent
            );
            mMessagesListAdapter.updateMessageContent(currentMessage);
        });
    }

    // 完成处理
    private void completeMessage(String msgId, String finalContent) {
        requireActivity().runOnUiThread(() -> {
            // 移除加载动画
            updateMessageContent(msgId, finalContent);
            input.setEnabled(true);
            input.setInputListener(createInputListener()); // 重新设置监听
        });
    }

    // 错误处理
    private void handleError(String msgId) {
        requireActivity().runOnUiThread(() -> {
            updateMessageContent(msgId, "请求失败，请重试");
            input.setEnabled(true);
            input.setInputListener(createInputListener());
        });
    }

    // 输入监听工厂方法
    private MessageInput.InputListener createInputListener() {
        return input -> {
            Message message = new Message("msg_" + System.currentTimeMillis(), mUser, input.toString());
            mMessagesListAdapter.addToStart(message, true);
            sendMessageToServer(input.toString());
            return true;
        };
    }
}
