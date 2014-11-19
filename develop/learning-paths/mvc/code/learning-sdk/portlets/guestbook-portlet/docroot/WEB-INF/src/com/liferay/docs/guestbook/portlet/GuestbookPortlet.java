package com.liferay.docs.guestbook.portlet;

import java.io.IOException;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.liferay.docs.guestbook.NoSuchGuestbookException;
import com.liferay.docs.guestbook.model.Entry;
import com.liferay.docs.guestbook.model.Guestbook;
import com.liferay.docs.guestbook.service.EntryLocalServiceUtil;
import com.liferay.docs.guestbook.service.GuestbookLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.OrderByComparatorFactory;
import com.liferay.portal.kernel.util.OrderByComparatorFactoryUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.ServiceContextFactory;
import com.liferay.portal.util.PortalUtil;
import com.liferay.util.bridges.mvc.MVCPortlet;

/**
 * Portlet implementation class GuestbookPortlet
 */
public class GuestbookPortlet extends MVCPortlet {

	public void addEntry(ActionRequest request, ActionResponse response)
			throws PortalException, SystemException {

		ServiceContext serviceContext = ServiceContextFactory.getInstance(
				Entry.class.getName(), request);

		String userName = ParamUtil.getString(request, "name");
		String email = ParamUtil.getString(request, "email");
		String message = ParamUtil.getString(request, "message");
		String guestbookName = ParamUtil.getString(request, "guestbookName");
		long entryId = ParamUtil.getLong(request, "entryId");
		
		OrderByComparatorFactory orderByComparatorFactory = OrderByComparatorFactoryUtil.getOrderByComparatorFactory();
		OrderByComparator orderByComparator = orderByComparatorFactory.create("guestbook", "name", true);
		
		Guestbook guestbook = GuestbookLocalServiceUtil.getGuestbookByName(guestbookName, orderByComparator);

		if (entryId > 0) {
			try {
				EntryLocalServiceUtil.updateEntry(serviceContext.getUserId(),
						guestbook.getGuestbookId(), entryId, userName, email, message,
						serviceContext);

				SessionMessages.add(request, "entryAdded");

				response.setRenderParameter("guestbookName",
						guestbook.getName());
			} catch (Exception e) {
				SessionErrors.add(request, e.getClass().getName());
				
				PortalUtil.copyRequestParameters(request, response);

				response.setRenderParameter("mvcPath",
						"/html/guestbook/edit_entry.jsp");
			}
		}
		else {
			try {
				EntryLocalServiceUtil.addEntry(serviceContext.getUserId(),
						guestbook.getGuestbookId(), userName, email, message, serviceContext);

				SessionMessages.add(request, "entryAdded");

				response.setRenderParameter("guestbookName",
						guestbook.getName());
			} catch (Exception e) {
				SessionErrors.add(request, e.getClass().getName());
				
				PortalUtil.copyRequestParameters(request, response);

				response.setRenderParameter("mvcPath",
						"/html/guestbook/edit_entry.jsp");
			}
		}
	}
	
	public void deleteEntry(ActionRequest request, ActionResponse response) {
		
		long entryId = ParamUtil.getLong(request, "entryId");
		long guestbookId = ParamUtil.getLong(request, "guestbookId");
		
		try {
			ServiceContext serviceContext = ServiceContextFactory.getInstance(
				Entry.class.getName(), request);
			
			response.setRenderParameter("guestbookId",
					Long.toString(guestbookId));

			EntryLocalServiceUtil.deleteEntry(entryId, serviceContext);
		} catch (Exception e) {
			
			SessionErrors.add(request, e.getClass().getName());
		}
	}

	public void addGuestbook(ActionRequest request, ActionResponse response)
			throws PortalException, SystemException {

		ServiceContext serviceContext = ServiceContextFactory.getInstance(
				Guestbook.class.getName(), request);

		String name = ParamUtil.getString(request, "guestbookName");

		try {
			GuestbookLocalServiceUtil.addGuestbook(serviceContext.getUserId(),
					name, serviceContext);

			SessionMessages.add(request, "guestbookAdded");

		} catch (Exception e) {
			SessionErrors.add(request, e.getClass().getName());

			response.setRenderParameter("mvcPath",
					"/html/guestbook/edit_guestbook.jsp");
		}
	}

	@Override
	public void render(RenderRequest renderRequest,
			RenderResponse renderResponse) throws PortletException, IOException {

		try {
			
			Guestbook guestbook = null;
			
			ServiceContext serviceContext = ServiceContextFactory.getInstance(
					Guestbook.class.getName(), renderRequest);
			
			String guestbookName = ParamUtil.getString(renderRequest, "guestbookName");

			long groupId = serviceContext.getScopeGroupId();

			List<Guestbook> guestbooks = GuestbookLocalServiceUtil
					.getGuestbooks(groupId);

			if (guestbooks.size() == 0) {
				guestbook = GuestbookLocalServiceUtil.addGuestbook(
						serviceContext.getUserId(), "Main", serviceContext);
			}

			else {
				
				if (!(renderRequest.getAttribute("guestbook") == null)) {
					guestbook = (Guestbook) renderRequest.getAttribute("guestbook");
				}
				
				else if (renderRequest.getAttribute("guestbook") == null && guestbookName.length() == 0) {
					guestbook = guestbooks.get(0);
				}
				
				else if (guestbookName.length() > 0) {
					OrderByComparatorFactory orderByComparatorFactory = OrderByComparatorFactoryUtil.getOrderByComparatorFactory();
					OrderByComparator orderByComparator = orderByComparatorFactory.create("guestbook", "name", true);
					
					guestbook = GuestbookLocalServiceUtil.getGuestbookByName(guestbookName, orderByComparator);
				}	
			}
			renderRequest.setAttribute("guestbook", guestbook);

		} catch (Exception e) {

			throw new PortletException(e);
		}

		super.render(renderRequest, renderResponse);
	}

	public void switchTabs (ActionRequest request, ActionResponse response) {
		
		OrderByComparatorFactory orderByComparatorFactory = OrderByComparatorFactoryUtil.getOrderByComparatorFactory();
		OrderByComparator orderByComparator = orderByComparatorFactory.create("guestbook", "name", true);
		
		String guestbookName = ParamUtil.getString(request, "guestbookName");
		
		try {
			Guestbook guestbook = GuestbookLocalServiceUtil.getGuestbookByName(guestbookName, orderByComparator);
			request.setAttribute("guestbook", guestbook);
		} catch (NoSuchGuestbookException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		response.setRenderParameter("mvcPath","/html/guestbook/view.jsp");
		
	}
	
}
