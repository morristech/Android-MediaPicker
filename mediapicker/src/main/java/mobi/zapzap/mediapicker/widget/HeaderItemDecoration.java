package mobi.zapzap.mediapicker.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import mobi.zapzap.mediapicker.MediaPickerUtil;

/** Created by Wade Morris on 2018/08/27. */
public class HeaderItemDecoration extends RecyclerView.ItemDecoration {

  private StickyHeaderInterface stickyHeaderInterface;
  private int stickyHeaderHeight;
  private Context context;

  public HeaderItemDecoration(@NonNull Context context, @NonNull StickyHeaderInterface listener) {

    this.context = context;
    this.stickyHeaderInterface = listener;
  }

  @Override
  public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {

    super.onDrawOver(c, parent, state);
    View topChild = parent.getChildAt(0);
    if (topChild == null) {
      return;
    }
    int topChildPosition = parent.getChildAdapterPosition(topChild);
    if (topChildPosition == RecyclerView.NO_POSITION) {
      return;
    }
    View currentHeader = getHeaderViewForItem(topChildPosition, parent);
    currentHeader.setPadding(
        (int) (currentHeader.getPaddingLeft() - MediaPickerUtil.convertPixelsToDp(5, context)),
        currentHeader.getPaddingTop(),
        currentHeader.getPaddingRight(),
        currentHeader.getPaddingBottom());
    fixLayoutSize(parent, currentHeader);
    int contactPoint = currentHeader.getBottom();
    View childInContact = getChildInContact(parent, contactPoint);
    if (null == childInContact) {
      //Timber.tag("childInContact").e("childInContact is null");
      return;
    }
    if (stickyHeaderInterface.isHeader(parent.getChildAdapterPosition(childInContact))) {
      moveHeader(c, currentHeader, childInContact);
      return;
    }
    drawHeader(c, currentHeader);
  }

  private View getHeaderViewForItem(int itemPosition, RecyclerView parent) {

    int headerPosition = stickyHeaderInterface.getHeaderPositionForItem(itemPosition);
    int layoutResId = stickyHeaderInterface.getHeaderLayout(headerPosition);
    View header = LayoutInflater.from(parent.getContext()).inflate(layoutResId, parent, false);
    stickyHeaderInterface.bindHeaderData(header, headerPosition);
    return header;
  }

  private void drawHeader(@NonNull Canvas c, @NonNull View header) {

    c.save();
    c.translate(0, 0);
    header.draw(c);
    c.restore();
  }

  private void moveHeader(@NonNull Canvas c, @NonNull View currentHeader, @NonNull View nextHeader) {

    c.save();
    c.translate(0, nextHeader.getTop() - currentHeader.getHeight());
    currentHeader.draw(c);
    c.restore();
  }

  private View getChildInContact(@NonNull RecyclerView parent, int contactPoint) {

    View childInContact = null;
    for (int i = 0; i < parent.getChildCount(); i++) {
      View child = parent.getChildAt(i);
      if (child.getBottom() > contactPoint) {
        if (child.getTop() <= contactPoint) {
          // This child overlaps the contactPoint
          childInContact = child;
          break;
        }
      }
    }
    return childInContact;
  }

  /**
   * Properly measures and layouts the top sticky header.
   *
   * @param parent ViewGroup: RecyclerView in this case.
   */
  private void fixLayoutSize(@NonNull ViewGroup parent, @NonNull View view) {

    // Specs for parent (RecyclerView)
    int widthSpec = View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.EXACTLY);
    int heightSpec = View.MeasureSpec.makeMeasureSpec(parent.getHeight(), View.MeasureSpec.UNSPECIFIED);
    // Specs for children (headers)
    int childWidthSpec = ViewGroup.getChildMeasureSpec(widthSpec, parent.getPaddingLeft() + parent.getPaddingRight(), view.getLayoutParams().width);
    int childHeightSpec = ViewGroup.getChildMeasureSpec(heightSpec,parent.getPaddingTop() + parent.getPaddingBottom(), view.getLayoutParams().height);
    view.measure(childWidthSpec, childHeightSpec);
    view.layout(0, 0, view.getMeasuredWidth(), stickyHeaderHeight = view.getMeasuredHeight());
  }

  public interface StickyHeaderInterface {

    /**
     * This method gets called by {@link HeaderItemDecoration} to fetch the position of the header
     * item in the adapter that is used for (represents) item at specified position.
     *
     * @param itemPosition int. Adapter's position of the item for which to do the search of the
     *     position of the header item.
     * @return int. Position of the header item in the adapter.
     */
    int getHeaderPositionForItem(int itemPosition);

    /**
     * This method gets called by {@link HeaderItemDecoration} to get layout resource id for the
     * header item at specified adapter's position.
     *
     * @param headerPosition int. Position of the header item in the adapter.
     * @return int. Layout resource id.
     */
    int getHeaderLayout(int headerPosition);

    /**
     * This method gets called by {@link HeaderItemDecoration} to setup the header View.
     *
     * @param header View. Header to set the data on.
     * @param headerPosition int. Position of the header item in the adapter.
     */
    void bindHeaderData(@NonNull View header, int headerPosition);

    /**
     * This method gets called by {@link HeaderItemDecoration} to verify whether the item represents
     * a header.
     *
     * @param itemPosition int.
     * @return true, if item at the specified adapter's position represents a header.
     */
    boolean isHeader(int itemPosition);
  }

}
