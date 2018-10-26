package com.abinbev.dsa.di.component;

import com.abinbev.dsa.activity.AppBaseActivity;
import com.abinbev.dsa.activity.AttachmentsListActivity;
import com.abinbev.dsa.activity.CasoEditActivity;
import com.abinbev.dsa.activity.PocAttachmentsActivity;
import com.abinbev.dsa.activity.ProductNegotiationDetailsActivity;
import com.abinbev.dsa.di.module.AppModule;
import com.abinbev.dsa.fragments.GiveGetSearchFragment;
import com.abinbev.dsa.fragments.NewPromotionDetailFragment;
import com.abinbev.dsa.service.AttachmentUploadService;
import com.abinbev.dsa.ui.presenter.AddProductForDistributionListPresenter;
import com.abinbev.dsa.ui.presenter.AddProductPresenter;
import com.abinbev.dsa.ui.presenter.AttachmentPresenter;
import com.abinbev.dsa.ui.presenter.CasoViewPresenter;
import com.abinbev.dsa.ui.presenter.PedidoListPresenter;
import com.abinbev.dsa.ui.presenter.ProductAllPresenter;
import com.abinbev.dsa.ui.presenter.ProductQuantityPresenter;
import com.abinbev.dsa.ui.presenter.SelectProductForDistributionListPresenter;
import com.abinbev.dsa.ui.view.Account360Header;
import com.abinbev.dsa.ui.view.negotiation.NegotiationDateHeaderView;
import com.abinbev.dsa.ui.view.negotiation.NegotiationItem;
import com.abinbev.dsa.ui.view.negotiation.NegotiationItemsView;
import com.abinbev.dsa.ui.view.negotiation.PackagePageView;
import dagger.Component;
import javax.inject.Singleton;

/**
 * Created by wandersonblough on 12/15/15.
 */
@Singleton
@Component(modules = {AppModule.class})
public interface AppComponent {
    void inject(AppBaseActivity activity);

    void inject(ProductNegotiationDetailsActivity activity);

    void inject(NewPromotionDetailFragment activity);

    void inject(NegotiationItem negotiationItem);

    void inject(PackagePageView packagePageView);

    void inject(NegotiationItemsView negotiationItemsView);

    void inject(NegotiationDateHeaderView headerView);

    void inject(AddProductPresenter addProductPresenter);

    void inject(ProductQuantityPresenter productQuantityPresenter);

    void inject(ProductAllPresenter productAllPresenter);

    void inject(PedidoListPresenter pedidoListPresenter);

    void inject(CasoViewPresenter casoViewPresenter);

    void inject(AttachmentUploadService attachmentUploadService);

    void inject(PocAttachmentsActivity pocAttachmentsActivity);

    void inject(AttachmentPresenter attachmentPresenter);

    void inject(AttachmentsListActivity attachmentsListActivity);

    void inject(CasoEditActivity casoEditActivity);

    void inject(GiveGetSearchFragment giveGetSearchFragment);

    void inject(Account360Header account360Header);

    void inject(SelectProductForDistributionListPresenter distributionPresenter);

    void inject(AddProductForDistributionListPresenter distributionPresenter);
}
