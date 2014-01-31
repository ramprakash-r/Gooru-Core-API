/////////////////////////////////////////////////////////////
// CollectionServiceImpl.java
// gooru-api
// Created by Gooru on 2014
// Copyright (c) 2014 Gooru. All rights reserved.
// http://www.goorulearning.org/
// Permission is hereby granted, free of charge, to any person obtaining
// a copy of this software and associated documentation files (the
// "Software"), to deal in the Software without restriction, including
// without limitation the rights to use, copy, modify, merge, publish,
// distribute, sublicense, and/or sell copies of the Software, and to
// permit persons to whom the Software is furnished to do so, subject to
// the following conditions:
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
// LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
// OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
// WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
/////////////////////////////////////////////////////////////
package org.ednovo.gooru.domain.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ednovo.gooru.core.api.model.ActionResponseDTO;
import org.ednovo.gooru.core.api.model.AssessmentQuestion;
import org.ednovo.gooru.core.api.model.Collection;
import org.ednovo.gooru.core.api.model.CollectionItem;
import org.ednovo.gooru.core.api.model.CollectionType;
import org.ednovo.gooru.core.api.model.Resource;
import org.ednovo.gooru.core.api.model.StorageArea;
import org.ednovo.gooru.core.api.model.User;
import org.ednovo.gooru.core.exception.NotFoundException;
import org.ednovo.gooru.infrastructure.persistence.hibernate.CollectionRepository;
import org.ednovo.gooru.infrastructure.persistence.hibernate.storage.StorageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CollectionServiceImpl extends ScollectionServiceImpl implements CollectionService {

	@Autowired
	private CollectionRepository collectionRepository;

	@Autowired
	private StorageRepository storageRepository;

	@Override
	public ActionResponseDTO<CollectionItem> createQuestionWithCollectionItem(String collectionId, String data, User user, String mediaFileName) throws Exception {
		ActionResponseDTO<CollectionItem> response = null;
		Collection collection = collectionRepository.getCollectionByGooruOid(collectionId, user.getGooruUId());
		if (collection == null) {
			throw new NotFoundException(generateErrorMessage(GL0056, _COLLECTION));
		}
		AssessmentQuestion question = getAssessmentService().buildQuestionFromInputParameters(data, user, true);
		question.setSharing(collection.getSharing());
		ActionResponseDTO<AssessmentQuestion> responseDTO = assessmentService.createQuestion(question, true);
		if (responseDTO.getModel() != null) {
			response = this.createCollectionItem(responseDTO.getModel().getGooruOid(), collectionId, new CollectionItem(), user, CollectionType.COLLECTION.getCollectionType(), true);
			if (mediaFileName != null && mediaFileName.length() > 0) {
				String questionImage = this.assessmentService.updateQuizQuestionImage(responseDTO.getModel().getGooruOid(), mediaFileName, question, ASSET_QUESTION);
				if (questionImage != null && questionImage.length() > 0) {
					response.getModel().setQuestionInfo(this.assessmentService.updateQuestionAssest(responseDTO.getModel().getGooruOid(), StringUtils.substringAfterLast(questionImage, "/")));
				}
			}
		}
		return response;

	}

	@Override
	public ActionResponseDTO<CollectionItem> moveCollectionToFolder(String sourceId, String taregetId, User user) throws Exception {
		ActionResponseDTO<CollectionItem> responseDTO = null;
		Collection source = collectionRepository.getCollectionByGooruOid(sourceId, user.getGooruUId());
		Collection target = collectionRepository.getCollectionByGooruOid(taregetId, user.getGooruUId());
		if (source == null || target == null) {
			throw new NotFoundException(generateErrorMessage("GL0056", "Collection"));
		}
		CollectionItem collectionItem = new CollectionItem();
		collectionItem.setCollection(source);
		responseDTO = this.createCollectionItem(sourceId, taregetId, collectionItem, user, CollectionType.FOLDER.getCollectionType(), false);
		return responseDTO;
	}

	@Override
	public ActionResponseDTO<CollectionItem> createQuestionWithCollectionItem(String collectionId, AssessmentQuestion assessmentQuestion, User user, String mediaFileName) throws Exception {

		ActionResponseDTO<CollectionItem> response = null;
		Collection collection = collectionRepository.getCollectionByGooruOid(collectionId, user.getGooruUId());
		if (collection == null) {
			throw new NotFoundException(generateErrorMessage("GL0056", "Collection"));
		}
		ActionResponseDTO<AssessmentQuestion> responseDTO = assessmentService.createQuestion(assessmentQuestion, true);
		if (responseDTO.getModel() != null) {
			response = this.createCollectionItem(responseDTO.getModel().getGooruOid(), collectionId, new CollectionItem(), user, CollectionType.COLLECTION.getCollectionType(), true);
			if (mediaFileName != null && mediaFileName.length() > 0) {
				String questionImage = this.assessmentService.updateQuizQuestionImage(responseDTO.getModel().getGooruOid(), mediaFileName, assessmentQuestion, "asset-question");
				if (questionImage != null && questionImage.length() > 0) {
					response.getModel().setQuestionInfo(this.assessmentService.updateQuestionAssest(responseDTO.getModel().getGooruOid(), StringUtils.substringAfterLast(questionImage, "/")));
				}
			}
		}
		return response;

	}

	@Override
	public List<Map<String, Object>> getMyShelf(String gooruUid, Integer limit, Integer offset, String sharing) {
		StorageArea storageArea = this.getStorageRepository().getStorageAreaByTypeName(NFS);
		List<Object[]> result = this.getCollectionRepository().getMyFolder(gooruUid, limit, offset, sharing);
		List<Map<String, Object>> folderList = new ArrayList<Map<String, Object>>();
		if (result != null && result.size() > 0) {
			for (Object[] object : result) {
				Map<String, Object> collection = new HashMap<String, Object>();
				collection.put(TITLE, object[0]);
				collection.put(GOORU_OID, object[1]);
				collection.put(TYPE, object[2]);
				if (object[4] != null) {
					Map<String, Object> thumbnails = new HashMap<String, Object>();
					thumbnails.put(URL, storageArea.getCdnDirectPath() + String.valueOf(object[3]) + String.valueOf(object[4]));
					collection.put(THUMBNAILS, thumbnails);
				}
				collection.put(COLLECTION_ITEMS, getFolderItem(String.valueOf(object[1]), sharing));
				collection.put(ITEM_COUNT, this.getCollectionRepository().getCollectionItemCount(String.valueOf(object[1]), sharing));
				collection.put(SHARING, object[5]);
				folderList.add(collection);
			}
		}
		return folderList;
	}

	public List<Map<String, Object>> getFolderItem(String gooruOid, String sharing) {
		List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
		List<Object[]> result = this.getCollectionRepository().getCollectionItem(gooruOid, 4, 0, false, sharing);
		if (result != null && result.size() > 0) {
			for (Object[] object : result) {
				Map<String, Object> item = new HashMap<String, Object>();
				item.put(TITLE, object[0]);
				item.put(GOORU_OID, object[1]);
				item.put(TYPE, object[2]);
				if (object[5] != null) {
					Map<String, Object> resourceFormat = new HashMap<String, Object>();
					resourceFormat.put(VALUE, object[5]);
					resourceFormat.put(DISPLAY_NAME, object[6]);
					item.put(RESOURCEFORMAT, resourceFormat);
				}
				item.put(SHARING, object[7]);
				items.add(item);
			}
			
		}
		return items;
	}

	@Override
	public List<Map<String, Object>> getFolderItems(String gooruOid, Integer limit, Integer offset, String sharing) {
		List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
		List<Object[]> result = this.getCollectionRepository().getCollectionItem(gooruOid, limit, offset, false, sharing);
		if (result != null && result.size() > 0) {
			for (Object[] object : result) {
				Map<String, Object> item = new HashMap<String, Object>();
				item.put(TITLE, object[0]);
				item.put(GOORU_OID, object[1]);
				item.put(TYPE, object[2]);
				if (object[5] != null) {
					Map<String, Object> resourceFormat = new HashMap<String, Object>();
					resourceFormat.put(VALUE, object[5]);
					resourceFormat.put(DISPLAY_NAME, object[6]);
					item.put(RESOURCEFORMAT, resourceFormat);
				}
				item.put(COLLECTION_ITEMS, getFolderItem(String.valueOf(object[1]), sharing));
				item.put(ITEM_COUNT, this.getCollectionRepository().getCollectionItemCount(String.valueOf(object[1]), sharing));
				item.put(SHARING, object[7]);
				items.add(item);
			}
		}
		return items;
	}
	@Override
	public Boolean resourceCopiedFrom(String gooruOid, String gooruUid) {
		Resource resource = collectionRepository.findResourceCopiedFrom(gooruOid, gooruUid);
		return resource != null ? true : false;
	}

	public StorageRepository getStorageRepository() {
		return storageRepository;
	}


}