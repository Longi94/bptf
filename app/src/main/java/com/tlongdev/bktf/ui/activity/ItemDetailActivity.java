/**
 * Copyright 2015 Long Tran
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tlongdev.bktf.ui.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.model.BackpackItem;
import com.tlongdev.bktf.model.Price;
import com.tlongdev.bktf.model.Quality;
import com.tlongdev.bktf.presenter.activity.ItemDetailPresenter;
import com.tlongdev.bktf.ui.view.activity.ItemDetailView;
import com.tlongdev.bktf.util.Utility;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * The (dialog) activity for showing info about an item in a backpack.
 */
public class ItemDetailActivity extends BptfActivity implements ItemDetailView {

    //Keys for the extra data in the intent
    public static final String EXTRA_ITEM_ID = "id";
    public static final String EXTRA_GUEST = "guest";
    public static final String EXTRA_ITEM_NAME = "name";
    public static final String EXTRA_ITEM_TYPE = "type";
    public static final String EXTRA_PROPER_NAME = "proper";

    @InjectExtra(EXTRA_GUEST) boolean isGuest;
    @InjectExtra(EXTRA_ITEM_ID) int mId;
    @InjectExtra(EXTRA_PROPER_NAME) int mProperName;
    @InjectExtra(EXTRA_ITEM_NAME) String mItemName;
    @InjectExtra(EXTRA_ITEM_TYPE) String mItemType;

    //References to all the text views in the view
    @Bind(R.id.text_view_name) TextView name;
    @Bind(R.id.text_view_level) TextView level;
    @Bind(R.id.text_view_effect_name) TextView effect;
    @Bind(R.id.text_view_custom_name) TextView customName;
    @Bind(R.id.text_view_custom_desc) TextView customDesc;
    @Bind(R.id.text_view_crafted) TextView crafterName;
    @Bind(R.id.text_view_gifted) TextView gifterName;
    @Bind(R.id.text_view_origin) TextView origin;
    @Bind(R.id.text_view_paint) TextView paint;
    @Bind(R.id.text_view_price) TextView priceView;
    @Bind(R.id.image_layout) FrameLayout layout;

    //References to the image view
    @Bind(R.id.icon) ImageView icon;
    @Bind(R.id.effect) ImageView effectView;
    @Bind(R.id.paint) ImageView paintView;
    @Bind(R.id.quality) ImageView quality;
    @Bind(R.id.card_view) CardView cardView;

    private ItemDetailPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);
        ButterKnife.bind(this);
        Dart.inject(this);

        mPresenter = new ItemDetailPresenter(mApplication);
        mPresenter.attachView(this);

        //Scale the icon, so the width of the image view is on third of the screen's width
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        layout.getLayoutParams().width = screenWidth / 3;
        layout.getLayoutParams().height = screenWidth / 3;
        layout.requestLayout();

        //Return to the previous activity if the user taps outside te dialog.
        ((View) cardView.getParent()).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 21)
                    finishAfterTransition();
                else
                    finish();
            }
        });
        //Do nothing if the user taps on the card view itself
        cardView.setOnClickListener(null);

        mPresenter.loadItemDetails(mId, isGuest);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
    }

    @Override
    public void showItemDetails(BackpackItem item) {
        item.setName(mItemName);
        //Set the name of the item
        name.setText(item.getFormattedName(this, mProperName == 1));

        //Set the level of the item, get the type from the intent
        if (item.getDefindex() >= 15000 && item.getDefindex() <= 15059) {
            level.setText(item.getDecoratedWeaponDesc(this, mItemType));
        } else {
            level.setText(getString(R.string.item_detail_level, item.getLevel(), mItemType));
        }

        //Set the origin of the item. Get the origin from the string array resource
        origin.setText(String.format("%s: %s", getString(R.string.item_detail_origin), Utility.getOriginName(this, item.getOrigin())));

        //Set the effect of the item (if any)
        if (item.getPriceIndex() != 0 && (item.getQuality() == Quality.UNUSUAL
                || item.getQuality() == Quality.COMMUNITY
                || item.getQuality() == Quality.SELF_MADE)) {
            effect.setText(String.format("%s: %s", getString(R.string.item_detail_effect),
                    Utility.getUnusualEffectName(this, item.getPriceIndex())));
            effect.setVisibility(View.VISIBLE);
        }

        //set the custom name of the item (if any)
        if (item.getCustomName() != null) {
            customName.setText(Html.fromHtml(String.format("%s: <i>%s</i>",
                    getString(R.string.item_detail_custom_name), item.getCustomName())));
            customName.setVisibility(View.VISIBLE);
        }

        //Set the custom description of the item (if any)
        if (item.getCustomDescription() != null) {
            customDesc.setText(Html.fromHtml(String.format("%s: <i>%s</i>",
                    getString(R.string.item_detail_custom_description), item.getCustomDescription())));
            customDesc.setVisibility(View.VISIBLE);
        }

        //Set the crafter's name (if any)
        if (item.getCreatorName() != null) {
            crafterName.setText(Html.fromHtml(String.format("%s: <i>%s</i>",
                    getString(R.string.item_detail_craft), item.getCreatorName())));
            crafterName.setVisibility(View.VISIBLE);
        }

        //Set the gifter's name (if any)
        if (item.getGifterName() != null) {
            gifterName.setText(Html.fromHtml(String.format("%s: <i>%s</i>",
                    getString(R.string.item_detail_gift), item.getGifterName())));
            gifterName.setVisibility(View.VISIBLE);
        }

        //Set the paint text (if any)
        if (item.getPaint() != 0) {
            paint.setText(String.format("%s: %s", getString(R.string.item_detail_paint), item.getPaintName(this)));
            paint.setVisibility(View.VISIBLE);
        }

        //Set the icon and the background
        Glide.with(this)
                .load(item.getIconUrl())
                .dontAnimate()
                .into(icon);

        if (item.getPriceIndex() != 0 && item.canHaveEffects()) {
            Glide.with(this)
                    .load(item.getEffectUrl())
                    .dontAnimate()
                    .into(effectView);
        }

        if (!item.isTradable()) {
            quality.setVisibility(View.VISIBLE);
            if (!item.isCraftable()) {
                quality.setImageResource(R.drawable.uncraft_untrad);
            } else {
                quality.setImageResource(R.drawable.untrad);
            }
        } else if (!item.isCraftable()) {
            quality.setVisibility(View.VISIBLE);
            quality.setImageResource(R.drawable.uncraft);
        }

        if (BackpackItem.isPaint(item.getPaint())) {
            Glide.with(this)
                    .load("file:///android_asset/paint/" + item.getPaint() + ".webp")
                    .into(paintView);
        }

        cardView.setCardBackgroundColor(item.getColor(this, true));

        Price price = item.getPrice();

        if (price != null)
        {
            //Show the priceView
            priceView.setVisibility(View.VISIBLE);
            priceView.setText(String.format("%s: %s",
                    getString(R.string.item_detail_suggested_price),
                    price.getFormattedPrice(this)));
        }
    }
}
