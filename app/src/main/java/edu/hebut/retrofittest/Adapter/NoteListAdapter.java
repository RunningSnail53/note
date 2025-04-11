package edu.hebut.retrofittest.Adapter;

import static edu.hebut.retrofittest.UI.MainActivity.login_user;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import edu.hebut.retrofittest.Bean.NoteBean;
import edu.hebut.retrofittest.DB.NoteDao;
import edu.hebut.retrofittest.R;
import edu.hebut.retrofittest.UI.EditActivity;
import edu.hebut.retrofittest.UI.NoteActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView适配器
 */

public class NoteListAdapter extends BaseAdapter
        implements View.OnClickListener {
    private Context mContext;

    private CharSequence[] chars = {"查看该记事", "编辑该记事", "删除该记事"};
    private List<NoteBean> mNotes;
    private OnRecyclerViewItemClickListener mOnItemClickListener;
    private int position;



    public int getPosition() {
        return position;
    }



    public NoteListAdapter() {
        mNotes = new ArrayList<>();
    }

    public void setmNotes(List<NoteBean> notes) {
        this.mNotes = notes;
        notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            //注意这里使用getTag方法获取数据
            mOnItemClickListener.onItemClick(v, (NoteBean) v.getTag());
        }
    }



    @Override
    public int getCount() {
        //Log.i(TAG, "###getItemCount: ");
        if (mNotes != null && mNotes.size() > 0) {
            return mNotes.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int i) {
        return mNotes.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, final ViewGroup viewGroup) {
        final int[] pos = new int[1];
        pos[0] = i;
        MyHolder holder = null;
        if (view == null) {
            holder = new MyHolder();
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_note, viewGroup, false);
            holder.itemView = view.findViewById(R.id.card_view_note);
            holder.tv_list_title = (TextView) view.findViewById(R.id.tv_list_title);
            holder.tv_list_summary = (TextView) view.findViewById(R.id.tv_list_summary);
            holder.tv_list_type = (TextView) view.findViewById(R.id.tv_list_type);
//            tv_list_mark= (TextView) view.findViewById(id.tv_list_mark);

            view.setTag(holder);
        }else {
            holder = (MyHolder) view.getTag();
        }
        final NoteBean note = mNotes.get(i);
        //将数据保存在itemView的Tag中，以便点击时进行获取
        //Log.e("adapter", "###record="+record);
        holder.tv_list_title.setText(note.getTitle());
        holder.tv_list_summary.setText(note.getContent());
        holder.tv_list_type.setText(note.getType());
//        holder.tv_list_mark.setText(note.getMark()==0?"未完成":"已完成");
//        holder.tv_list_mark.setTextColor(note.getMark()==0?0xFF666666:0xFF9E9E9E);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(viewGroup.getContext(), NoteActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("note", note);
                intent.putExtra("data", bundle);
                viewGroup.getContext().startActivity(intent);
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                NoteListAdapter.this.position = pos[0];
                AlertDialog dialog = new AlertDialog.Builder(viewGroup.getContext())
                        .setItems(chars, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                switch (i) {
                                    case 0://查看该记事
                                        Intent intent = new Intent(viewGroup.getContext(), NoteActivity.class);
                                        Bundle bundle = new Bundle();
                                        bundle.putSerializable("note", note);
                                        intent.putExtra("data", bundle);
                                        viewGroup.getContext().startActivity(intent);
                                        break;

                                    case 1://编辑该记事
                                        Intent intent2 = new Intent(viewGroup.getContext(), EditActivity.class);
                                        Bundle bundle2 = new Bundle();
                                        bundle2.putSerializable("note",note);
                                        intent2.putExtra("data", bundle2);
                                        intent2.putExtra("flag", 1);//编辑记事
                                        viewGroup.getContext().startActivity(intent2);
                                        break;

                                    case 2://删除该记事
                                        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(viewGroup.getContext());
                                        builder.setTitle("提示");
                                        builder.setMessage("确定删除记事？");
                                        builder.setCancelable(false);
                                        final int finalPosition = position;
                                        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                NoteDao noteDao = new NoteDao(viewGroup.getContext());
                                                int ret = noteDao.DeleteNote(note.getId());
                                                if (ret > 0) {
                                                    Toast.makeText(viewGroup.getContext(), "删除成功", Toast.LENGTH_SHORT).show();
                                                   List noteList = noteDao.queryNotesAll(login_user);

                                                  setmNotes(noteList);
                                                  notifyDataSetChanged();
                                                }
                                            }
                                        });
                                        builder.setNegativeButton("取消", null);
                                        builder.create().show();
                                        break;




                                }
                            }
                        }).show();
                return false;
            }
        });
        return view;
    }

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, NoteBean note);
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }






    public class MyHolder{
        public View itemView;
        public TextView tv_list_title;//记事标题
        public TextView tv_list_summary;//记事摘要
        public TextView tv_list_type;//记事类型
        public TextView tv_list_mark;

    }

}
