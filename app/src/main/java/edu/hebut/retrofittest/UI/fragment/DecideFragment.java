package edu.hebut.retrofittest.UI.fragment;


import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import edu.hebut.retrofittest.Bean.Message;
import edu.hebut.retrofittest.Bean.User;
import edu.hebut.retrofittest.R;
import edu.hebut.retrofittest.Util.EnCodeUtils;
import edu.hebut.retrofittest.assistant.client.RetrofitClient;
import edu.hebut.retrofittest.assistant.entity.ChatRequest;
import edu.hebut.retrofittest.assistant.entity.ChatResponse;
import edu.hebut.retrofittest.assistant.service.ChatApi;

public class DecideFragment extends Fragment implements MessagesListAdapter.OnLoadMoreListener {

    private static final String USER_ID = "0";
    private static final String AI_ASSISTANT_ID = "1";
    private static final String AI_ASSISTANT_AVATAR = "https://img.6tu.com/2021/11/20211103054112556.jpg";

    private User mUser;
    private User mAiAssistant;
    private MessagesListAdapter<Message> mMessagesListAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUser = new User(USER_ID, "User", null, true);

        mAiAssistant = new User(AI_ASSISTANT_ID, "AiAssistant", AI_ASSISTANT_AVATAR, true);

        /*
         * senderId:自己的id，用于区分自己和对方，控制消息气泡的位置。
         * imageLoader:图像加载器
         *
         * */
        mMessagesListAdapter = new MessagesListAdapter<>(USER_ID, (imageView, url, payload) -> Picasso.get().load(url).into(imageView));

        //滑倒顶部时加载历史记录
        mMessagesListAdapter.setLoadMoreListener(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // 实例fragment_decision布局
        View fgDecide = inflater.inflate(R.layout.fragment_decide, container, false);

        // 获取消息列表和输入框
        MessagesList messagesList = fgDecide.findViewById(R.id.messagesList);
        MessageInput input = fgDecide.findViewById(R.id.input);

        //发送输入框中的文本，addToStart的第二个参数是列表滚动到底部
        input.setInputListener(input1 -> {
            Message message = new Message(USER_ID, mUser, input1.toString());
            mMessagesListAdapter.addToStart(message, true);
            sendMessageToServer("0", input1.toString());
            return true;
        });

        //小加号按钮点击事件
        input.setAttachmentsListener(() -> {
            // TODO: 处理附件点击事件
            mMessagesListAdapter.addToStart(new Message(AI_ASSISTANT_ID, mAiAssistant, "添加附件功能待实现"), true);
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

    /**
     * 发送消息到服务器
     *
     * @param userId  用户ID
     * @param message 消息内容
     */
    private void sendMessageToServer(String userId, String message) {
        // 发送消息到服务器
        ChatApi chatApi = RetrofitClient.getInstance().create(ChatApi.class);
        ChatRequest chatRequest = new ChatRequest(userId, message);

        // 使用Retrofit发送请求
        chatApi.sendMessage(chatRequest).enqueue(new retrofit2.Callback<ChatResponse>() {
            @Override
            public void onResponse(retrofit2.Call<ChatResponse> call, retrofit2.Response<ChatResponse> response) {
                if (response.isSuccessful()) {
                    // 处理服务器返回的消息
                    ChatResponse chatResponse = response.body();

                    if (chatResponse != null) {
                        String reply = chatResponse.getReply();
                        mMessagesListAdapter.addToStart(new Message(AI_ASSISTANT_ID, mAiAssistant, reply), true);
                        ;
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ChatResponse> call, Throwable t) {
                // 处理错误
                t.printStackTrace();
            }
        });
    }
}
