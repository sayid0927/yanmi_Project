package com.easemob.easeui.widget.chatrow;

import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.NetworkImageView;
import com.easemob.EMCallBack;
import com.easemob.EMError;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMMessage.Direct;
import com.easemob.chatuidemo.HXConstant;
import com.easemob.chatuidemo.R;
import com.easemob.easeui.EaseConstant;
import com.easemob.easeui.adapter.EaseMessageAdapter;
import com.easemob.easeui.utils.EaseUserUtils;
import com.easemob.easeui.widget.EaseChatMessageList;
import com.easemob.easeui.widget.EaseChatMessageList.MessageListItemClickListener;
import com.easemob.exceptions.EaseMobException;
import com.easemob.util.DateUtils;

public abstract class EaseChatRow extends LinearLayout {
    protected static final String TAG = EaseChatRow.class.getSimpleName();

    protected LayoutInflater inflater;
    protected Context context;
    protected BaseAdapter adapter;
    protected EMMessage message;
    protected int position;

    protected TextView timeStampView;
    protected NetworkImageView userAvatarView;
    protected View bubbleLayout;
    protected TextView usernickView;

    protected TextView percentageView;
    protected ProgressBar progressBar;
    protected ImageView statusView;
    protected Activity activity;

    protected TextView ackedView;
    protected TextView deliveredView;

    protected EMCallBack messageSendCallback;
    protected EMCallBack messageReceiveCallback;
    private boolean isTimestamp=true;

    protected MessageListItemClickListener itemClickListener;

    public EaseChatRow(Context context, EMMessage message, int position, BaseAdapter adapter) {
        super(context);
        this.context = context;
        this.activity = (Activity) context;
        this.message = message;
        this.position = position;
        this.adapter = adapter;
        inflater = LayoutInflater.from(context);

        initView();
    }

    private void initView() {
        onInflatView();
        timeStampView = (TextView) findViewById(R.id.timestamp);
        userAvatarView = (NetworkImageView) findViewById(R.id.iv_userhead);
        bubbleLayout = findViewById(R.id.bubble);
        usernickView = (TextView) findViewById(R.id.tv_userid);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        statusView = (ImageView) findViewById(R.id.msg_status);
        ackedView = (TextView) findViewById(R.id.tv_ack);
        deliveredView = (TextView) findViewById(R.id.tv_delivered);

        onFindViewById();
    }

    /**
     * 根据当前message和position设置控件属性等
     *
     * @param message
     * @param position
     */
    public void setUpView(EMMessage message, int position,
                          EaseChatMessageList.MessageListItemClickListener itemClickListener) {
        this.message = message;
        this.position = position;
        this.itemClickListener = itemClickListener;

        setUpBaseView();
        onSetUpView();
        setClickListener();
    }

    public void isTimestamp(boolean isTimestamp){
        this.isTimestamp=isTimestamp;
    }

    private void setUpBaseView() {
        // 设置用户昵称头像，bubble背景等
        if (timeStampView != null&&isTimestamp) {
            if (position == 0) {
                timeStampView.setText(EaseConstant.getTimestampString(new Date(message.getMsgTime())));
                timeStampView.setVisibility(View.VISIBLE);
            } else {
                // 两条消息时间离得如果稍长，显示时间
                EMMessage prevMessage = (EMMessage) adapter.getItem(position - 1);
                if (prevMessage != null &&
                        DateUtils.isCloseEnough(message.getMsgTime(), prevMessage.getMsgTime())) {
                    timeStampView.setVisibility(View.GONE);
                } else {
                    timeStampView.setText(EaseConstant.getTimestampString(new Date(message.getMsgTime())));
                    timeStampView.setVisibility(View.VISIBLE);
                }
            }
        }else if(timeStampView!=null){
            timeStampView.setVisibility(View.GONE);
        }


        if (!message.getFrom().contains(HXConstant.SYS_MSG)) {  //系统消息
                EaseUserUtils.setUserNickAndAvatar2(message, usernickView, userAvatarView);
        }


        if (deliveredView != null) {
            if (message.isDelivered) {
                deliveredView.setVisibility(View.VISIBLE);
            } else {
                deliveredView.setVisibility(View.INVISIBLE);
            }
        }

        if (ackedView != null) {
            if (message.isAcked) {
                if (deliveredView != null) {
                    deliveredView.setVisibility(View.INVISIBLE);
                }
                ackedView.setVisibility(View.VISIBLE);
            } else {
                ackedView.setVisibility(View.INVISIBLE);
            }
        }


        if (adapter instanceof EaseMessageAdapter) {
            if (userAvatarView != null) {
                if (((EaseMessageAdapter) adapter).isShowAvatar()) {
                    userAvatarView.setVisibility(View.VISIBLE);
                } else {
                    userAvatarView.setVisibility(View.GONE);
                }
            }
            if (usernickView != null) {
                if (((EaseMessageAdapter) adapter).isShowUserNick()) {
                    usernickView.setVisibility(View.VISIBLE);
                } else {
                    usernickView.setVisibility(View.GONE);
                }
            }
            if (message.direct == Direct.SEND) {
                if (((EaseMessageAdapter) adapter).getMyBubbleBg() != null) {
                    bubbleLayout.setBackgroundDrawable(((EaseMessageAdapter) adapter).getMyBubbleBg());
                }
                // else
                // bubbleLayout.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.chatto_bg));
            } else if (message.direct == Direct.RECEIVE) {
                if (((EaseMessageAdapter) adapter).getOtherBuddleBg() != null) {
                    bubbleLayout.setBackgroundDrawable(((EaseMessageAdapter) adapter).getOtherBuddleBg());
                }
                //                else
                //                    bubbleLayout.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.ease_chatfrom_bg));
            }
        }
    }

    /**
     * 设置消息发送callback
     */
    protected void setMessageSendCallback() {
        if (messageSendCallback == null) {
            messageSendCallback = new EMCallBack() {

                @Override
                public void onSuccess() {
                    updateView();
                }

                @Override
                public void onProgress(final int progress, String status) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (percentageView != null) {
                                percentageView.setText(progress + "%");
                            }

                        }
                    });
                }

                @Override
                public void onError(int code, String error) {
                    updateView();
                }
            };
        }
        message.setMessageStatusCallback(messageSendCallback);
    }

    /**
     * 设置消息接收callback
     */
    protected void setMessageReceiveCallback() {
        if (messageReceiveCallback == null) {
            messageReceiveCallback = new EMCallBack() {

                @Override
                public void onSuccess() {
                    updateView();
                }

                @Override
                public void onProgress(final int progress, String status) {
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            if (percentageView != null) {
                                percentageView.setText(progress + "%");
                            }
                        }
                    });
                }

                @Override
                public void onError(int code, String error) {
                    updateView();
                }
            };
        }
        message.setMessageStatusCallback(messageReceiveCallback);
    }


    private void setClickListener() {
        if (bubbleLayout != null) {
            bubbleLayout.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (itemClickListener != null) {
                        if (!itemClickListener.onBubbleClick(message)) {
                            //如果listener返回false不处理这个事件，执行lib默认的处理
                            onBubbleClick();
                        }
                    }
                }
            });

            bubbleLayout.setOnLongClickListener(new OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    if (itemClickListener != null) {
                        itemClickListener.onBubbleLongClick(message);
                    }
                    return true;
                }
            });
        }

        if (statusView != null) {
            statusView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (itemClickListener != null) {
                        itemClickListener.onResendClick(message);
                    }
                }
            });
        }

        if (userAvatarView != null) {
            userAvatarView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (itemClickListener != null) {
                        if (message.direct == Direct.SEND) {
                            itemClickListener.onUserAvatarClick(EMChatManager.getInstance().getCurrentUser());
                        } else {
                            itemClickListener.onUserAvatarClick(message.getFrom());
                        }
                    }
                }
            });
        }
    }


    protected void updateView() {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                if (message.status == EMMessage.Status.FAIL) {

                    if (message.getError() == EMError.MESSAGE_SEND_INVALID_CONTENT) {
                        Toast.makeText(activity, activity.getString(R.string.send_fail) +
                                activity.getString(R.string.error_send_invalid_content), 0).show();
                    } else if (message.getError() == EMError.MESSAGE_SEND_NOT_IN_THE_GROUP) {
                        Toast.makeText(activity, activity.getString(R.string.send_fail) +
                                activity.getString(R.string.error_send_not_in_the_group), 0).show();
                    } else {
                        Toast.makeText(activity, activity.getString(R.string.send_fail) +
                                activity.getString(R.string.connect_failuer_toast), 0).show();
                    }
                }

                onUpdateView();
            }
        });

    }

    /**
     * 填充layout
     */
    protected abstract void onInflatView();

    /**
     * 查找chatrow里的控件
     */
    protected abstract void onFindViewById();

    /**
     * 消息状态改变，刷新listview
     */
    protected abstract void onUpdateView();

    /**
     * 设置更新控件属性
     */
    protected abstract void onSetUpView();

    /**
     * 聊天气泡被点击事件
     */
    public abstract void onBubbleClick();

}
