package net.i2p.android.i2ptunnel;

import android.content.Context;
import android.os.Build;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.i2p.android.router.R;
import net.i2p.android.util.FragmentUtils;

import java.util.List;

public class TunnelEntryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mCtx;
    private boolean mClientTunnels;
    private TunnelListFragment.OnTunnelSelectedListener mListener;
    private FragmentUtils.TwoPaneProvider mTwoPane;
    private List<TunnelEntry> mTunnels;
    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = -1;

    public static class SimpleViewHolder extends RecyclerView.ViewHolder {
        public SimpleViewHolder(View itemView) {
            super(itemView);
        }
    }

    public static class TunnelViewHolder extends RecyclerView.ViewHolder {
        public ImageView status;
        public TextView name;
        public TextView description;
        public TextView interfacePort;

        public TunnelViewHolder(View itemView) {
            super(itemView);

            status = (ImageView) itemView.findViewById(R.id.tunnel_status);
            name = (TextView) itemView.findViewById(R.id.tunnel_name);
            description = (TextView) itemView.findViewById(R.id.tunnel_description);
            interfacePort = (TextView) itemView.findViewById(R.id.tunnel_interface_port);
        }
    }

    public TunnelEntryAdapter(Context context, boolean clientTunnels,
                              TunnelListFragment.OnTunnelSelectedListener listener,
                              FragmentUtils.TwoPaneProvider twoPane) {
        super();
        mCtx = context;
        mClientTunnels = clientTunnels;
        mListener = listener;
        mTwoPane = twoPane;
    }

    public void setTunnels(List<TunnelEntry> tunnels) {
        mTunnels = tunnels;
        notifyDataSetChanged();
    }

    public void addTunnel(TunnelEntry tunnel) {
        boolean wasEmpty = mTunnels.isEmpty();
        mTunnels.add(tunnel);
        if (wasEmpty) {
            notifyDataSetChanged();
        } else {
            notifyItemInserted(mTunnels.size() - 1);
        }
    }

    public TunnelEntry getTunnel(int position) {
        if (position < 0)
            return null;

        return mTunnels.get(position);
    }

    public void setActivatedPosition(int position) {
        mActivatedPosition = position;
    }

    public int getActivatedPosition() {
        return mActivatedPosition;
    }

    public void clearActivatedPosition() {
        mActivatedPosition = -1;
    }

    @Override
    public int getItemViewType(int position) {
        if (mTunnels == null)
            return R.string.router_not_running;
        else if (mTunnels.isEmpty())
            return R.layout.listitem_empty;
        else
            return R.layout.listitem_i2ptunnel;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int vt = viewType;
        if (viewType == R.string.router_not_running)
            vt = R.layout.listitem_empty;

        View v = LayoutInflater.from(parent.getContext())
                .inflate(vt, parent, false);
        switch (viewType) {
            case R.layout.listitem_i2ptunnel:
                return new TunnelViewHolder(v);
            default:
                return new SimpleViewHolder(v);
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        switch (holder.getItemViewType()) {
            case R.string.router_not_running:
                ((TextView) holder.itemView).setText(
                        mCtx.getString(R.string.router_not_running));
                break;

            case R.layout.listitem_empty:
                ((TextView) holder.itemView).setText(mClientTunnels ?
                        R.string.no_configured_client_tunnels :
                        R.string.no_configured_server_tunnels);
                break;

            case R.layout.listitem_i2ptunnel:
                final TunnelViewHolder tvh = (TunnelViewHolder) holder;
                final TunnelEntry tunnel = getTunnel(position);

                tvh.status.setImageDrawable(tunnel.getStatusIcon());
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
                    tvh.status.setBackgroundDrawable(tunnel.getStatusBackground());
                else
                    tvh.status.setBackground(tunnel.getStatusBackground());
                ViewCompat.setTransitionName(tvh.status,
                        "status" + tunnel.getId());

                tvh.name.setText(tunnel.getName());
                tvh.description.setText(tunnel.getDescription());
                tvh.interfacePort.setText(tunnel.getTunnelLink(false));

                tvh.itemView.setSelected(mTwoPane.isTwoPane() && position == mActivatedPosition);
                tvh.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int oldPosition = mActivatedPosition;
                        mActivatedPosition = position;
                        notifyItemChanged(oldPosition);
                        notifyItemChanged(position);
                        Pair<View, String> statusPair = Pair.create(
                                (View)tvh.status,
                                ViewCompat.getTransitionName(tvh.status));
                        Pair<View, String>[] pairs = new Pair[]{ statusPair};
                        mListener.onTunnelSelected(tunnel.getId(), pairs);
                    }
                });
                break;

            default:
                break;
        }
    }

    // Return the size of the dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (mTunnels == null || mTunnels.isEmpty())
            return 1;

        return mTunnels.size();
    }
}
