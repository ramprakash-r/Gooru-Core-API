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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ednovo.gooru.core.api.model.ActionResponseDTO;
import org.ednovo.gooru.core.api.model.AssessmentQuestion;
import org.ednovo.gooru.core.api.model.Code;
import org.ednovo.gooru.core.api.model.Collection;
import org.ednovo.gooru.core.api.model.CollectionItem;
import org.ednovo.gooru.core.api.model.CollectionType;
import org.ednovo.gooru.core.api.model.Resource;
import org.ednovo.gooru.core.api.model.SessionContextSupport;
import org.ednovo.gooru.core.api.model.ShelfType;
import org.ednovo.gooru.core.api.model.StorageArea;
import org.ednovo.gooru.core.api.model.User;
import org.ednovo.gooru.core.exception.NotFoundException;
import org.ednovo.gooru.domain.service.search.SearchResults;
import org.ednovo.gooru.infrastructure.persistence.hibernate.CollectionRepository;
import org.ednovo.gooru.infrastructure.persistence.hibernate.storage.StorageRepository;
import org.ednovo.gooru.infrastructure.persistence.hibernate.taxonomy.TaxonomyRespository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

@Service
public class CollectionServiceImpl extends ScollectionServiceImpl implements CollectionService {

	@Autowired
	private CollectionRepository collectionRepository;

	@Autowired
	private TaxonomyRespository taxonomyRespository;

	@Autowired
	private StorageRepository storageRepository;

	private static int collectionItemcount = 0;

	@Override
	public ActionResponseDTO<CollectionItem> createQuestionWithCollectionItem(String collectionId, String data, User user, String mediaFileName) throws Exception {
		ActionResponseDTO<CollectionItem> response = null;
		Collection collection = collectionRepository.getCollectionByGooruOid(collectionId, null);
		if (collection == null) {
			throw new NotFoundException(generateErrorMessage(GL0056, _COLLECTION));
		}
		AssessmentQuestion question = getAssessmentService().buildQuestionFromInputParameters(data, user, true);
		question.setSharing(collection.getSharing());
		ActionResponseDTO<AssessmentQuestion> responseDTO = assessmentService.createQuestion(question, true);
		if (responseDTO.getModel() != null) {
			response = this.createCollectionItem(responseDTO.getModel(), collection, user);
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
		Collection source = collectionRepository.getCollectionByGooruOid(sourceId, null);
		if (source == null) {
			throw new NotFoundException(generateErrorMessage("GL0056", "Collection"));
		}
		CollectionItem collectionItem = new CollectionItem();
		collectionItem.setCollection(source);
		CollectionItem sourceCollectionItem = this.getCollectionRepository().findCollectionItemByGooruOid(sourceId, user.getPartyUid());
		String itemType = sourceCollectionItem.getItemType();
		collectionItem.setItemType(itemType);
		if (sourceCollectionItem != null) {
			deleteCollectionItem(sourceCollectionItem.getCollectionItemId(), user);
		}
		if (taregetId != null) {
			responseDTO = this.createCollectionItem(sourceId, taregetId, collectionItem, user, CollectionType.FOLDER.getCollectionType(), false);
		} else {
			responseDTO = this.createCollectionItem(sourceId, null, collectionItem, user, CollectionType.SHElf.getCollectionType(), false);
		}

		return responseDTO;
	}

	@Override
	public ActionResponseDTO<CollectionItem> createQuestionWithCollectionItem(String collectionId, AssessmentQuestion assessmentQuestion, User user, String mediaFileName) throws Exception {

		ActionResponseDTO<CollectionItem> response = null;
		Collection collection = collectionRepository.getCollectionByGooruOid(collectionId, null);
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
	public List<CollectionItem> createCollectionItems(List<String> collectionsIds, String resourceId, User user) throws Exception {
		Collection collection = this.getCollectionRepository().getCollectionByGooruOid(resourceId, null);
		if (collection == null) {
			throw new NotFoundException(generateErrorMessage(GL0056, COLLECTION));
		}
		List<CollectionItem> collectionItems = new ArrayList<CollectionItem>();
		for (String collectionId : collectionsIds) {
			Collection classPage = this.getCollectionRepository().getCollectionByGooruOid(collectionId, null);
			if (classPage != null) {
				CollectionItem collectionItem = new CollectionItem();
				collectionItem.setCollection(classPage);
				collectionItem.setResource(collection);
				collectionItem.setItemType(ShelfType.AddedType.ADDED.getAddedType());
				collectionItem.setAssociatedUser(user);
				collectionItem.setAssociationDate(new Date(System.currentTimeMillis()));
				int sequence = collectionItem.getCollection().getCollectionItems() != null ? collectionItem.getCollection().getCollectionItems().size() + 1 : 1;
				collectionItem.setItemSequence(sequence);
				this.getResourceRepository().save(collectionItem);
				collectionItems.add(collectionItem);
				SessionContextSupport.putLogParameter(EVENT_NAME, CLASSPAGE_CREATE_COLLECTION_TASK_ITEM);
				SessionContextSupport.putLogParameter(COLLECTION_ITEM_ID, collectionItem.getCollectionItemId());
				SessionContextSupport.putLogParameter(GOORU_OID, classPage.getGooruOid());
				SessionContextSupport.putLogParameter(COLLECTION_ID, classPage.getGooruOid());
				SessionContextSupport.putLogParameter(RESOURCE_ID, collection.getGooruOid());
				SessionContextSupport.putLogParameter(COLLECTION_TYPE, collectionItem.getCollection().getCollectionType());
			}

		}
		return collectionItems;

	}

	@Override
	public List<Map<String, Object>> getMyShelf(String gooruUid, Integer limit, Integer offset, String sharing, String collectionType, Integer itemLimit, boolean fetchChildItem) {
		StorageArea storageArea = this.getStorageRepository().getStorageAreaByTypeName(NFS);
		List<Object[]> result = this.getCollectionRepository().getMyFolder(gooruUid, limit, offset, sharing, fetchChildItem ? FOLDER : collectionType);
		List<Map<String, Object>> folderList = new ArrayList<Map<String, Object>>();
		int count = 0;
		if (result != null && result.size() > 0) {
			for (Object[] object : result) {
				collectionItemcount = 0;
				Map<String, Object> collection = new HashMap<String, Object>();
				collection.put(TITLE, object[0]);
				collection.put(GOORU_OID, object[1]);
				collection.put(TYPE, object[2]);
				if (object[4] != null) {
					Map<String, Object> thumbnails = new HashMap<String, Object>();
					thumbnails.put(URL, storageArea.getCdnDirectPath() + String.valueOf(object[3]) + String.valueOf(object[4]));
					collection.put(THUMBNAILS, thumbnails);
				}
				if (fetchChildItem) {
					if (count == 0) {
						collection.put(COLLECTION_ITEMS, getFolderItem(String.valueOf(object[1]), sharing, String.valueOf(object[2]), collectionType, itemLimit, fetchChildItem));
					}
				} else {
					collection.put(COLLECTION_ITEMS, getFolderItem(String.valueOf(object[1]), sharing, String.valueOf(object[2]), collectionType, itemLimit, fetchChildItem));
				}
				collection.put(ITEM_COUNT, this.getCollectionRepository().getCollectionItemCount(String.valueOf(object[1]), sharing, collectionType));
				collection.put(SHARING, object[5]);
				collection.put(COLLECTION_ITEM_ID, object[6]);
				if (object[7] != null) {
					collection.put(GOALS, object[7]);
				}

				if (object[8] != null) {
					Map<String, Object> resourceFormat = new HashMap<String, Object>();
					resourceFormat.put(VALUE, object[8]);
					resourceFormat.put(DISPLAY_NAME, object[9]);
					collection.put(RESOURCEFORMAT, resourceFormat);
				}

				if (object[10] != null) {
					Map<String, Object> resourceSource = new HashMap<String, Object>();
					resourceSource.put(ATTRIBUTION, object[10]);
					resourceSource.put(DOMAIN_NAME, object[11]);
					collection.put(RESOURCESOURCE, resourceSource);
				}
				count++;
				folderList.add(collection);
			}
		}
		return folderList;
	}

	public List<Map<String, Object>> getFolderItem(String gooruOid, String sharing, String type, String collectionType, Integer itemLimit, boolean fetchChildItem) {
		StorageArea storageArea = this.getStorageRepository().getStorageAreaByTypeName(NFS);
		List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
		List<Object[]> result = this.getCollectionRepository().getCollectionItem(gooruOid, type.equalsIgnoreCase(SCOLLECTION) ? 4 : itemLimit, 0, false, sharing, type.equalsIgnoreCase(SCOLLECTION) ? SEQUENCE : null, collectionType);
		if (result != null && result.size() > 0) {
			if (fetchChildItem) {
				if (type.equalsIgnoreCase(FOLDER)) {
					collectionItemcount = 0;
				}
			}
			for (Object[] object : result) {
				Map<String, Object> item = new HashMap<String, Object>();
				item.put(TITLE, object[0]);
				item.put(GOORU_OID, object[1]);
				item.put(TYPE, object[2]);
				if (object[4] != null) {
					Map<String, Object> thumbnails = new HashMap<String, Object>();
					thumbnails.put(URL, storageArea.getCdnDirectPath() + String.valueOf(object[3]) + String.valueOf(object[4]));
					item.put(THUMBNAILS, thumbnails);
				}
				if (object[5] != null) {
					Map<String, Object> resourceFormat = new HashMap<String, Object>();
					resourceFormat.put(VALUE, object[5]);
					resourceFormat.put(DISPLAY_NAME, object[6]);
					item.put(RESOURCEFORMAT, resourceFormat);
				}
				item.put(SHARING, object[7]);
				item.put(COLLECTION_ITEM_ID, object[8]);
				if (fetchChildItem && (String.valueOf(object[2]).equalsIgnoreCase(SCOLLECTION) || String.valueOf(object[2]).equalsIgnoreCase(FOLDER))) {
					if (String.valueOf(object[2]).equalsIgnoreCase(SCOLLECTION)) {
						if (collectionItemcount == 0) {
							item.put(COLLECTION_ITEMS, getFolderItem(String.valueOf(object[1]), sharing, String.valueOf(object[2]), collectionType, type.equalsIgnoreCase(SCOLLECTION) ? 4 : itemLimit, fetchChildItem));
							item.put(ITEM_COUNT, this.getCollectionRepository().getCollectionItemCount(String.valueOf(object[1]), sharing, collectionType));
							collectionItemcount++;
						}
					} else if ((String.valueOf(object[2]).equalsIgnoreCase(SCOLLECTION) || String.valueOf(object[2]).equalsIgnoreCase(FOLDER))) {
						item.put(COLLECTION_ITEMS, getFolderItem(String.valueOf(object[1]), sharing, String.valueOf(object[2]), collectionType, type.equalsIgnoreCase(SCOLLECTION) ? 4 : itemLimit, fetchChildItem));
						item.put(ITEM_COUNT, this.getCollectionRepository().getCollectionItemCount(String.valueOf(object[1]), sharing, collectionType));
					}
				}
				if (object[9] != null) {
					item.put(GOALS, object[9]);
				}
				if (object[10] != null) {
					Map<String, Object> resourceSource = new HashMap<String, Object>();
					resourceSource.put(ATTRIBUTION, object[10]);
					resourceSource.put(DOMAIN_NAME, object[11]);
					item.put(RESOURCESOURCE, resourceSource);
				}
				items.add(item);
			}

		}
		return items;
	}

	@Override
	public List<Map<String, Object>> getFolderItems(String gooruOid, Integer limit, Integer offset, String sharing, String collectionType, String orderBy, Integer itemLimit, boolean fetchChildItem) {
		StorageArea storageArea = this.getStorageRepository().getStorageAreaByTypeName(NFS);
		List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
		List<Object[]> result = this.getCollectionRepository().getCollectionItem(gooruOid, limit, offset, false, sharing, orderBy, collectionType);
		if (result != null && result.size() > 0) {
			for (Object[] object : result) {
				collectionItemcount = 0;
				Map<String, Object> item = new HashMap<String, Object>();
				item.put(TITLE, object[0]);
				item.put(GOORU_OID, object[1]);
				item.put(TYPE, object[2]);
				if (object[4] != null) {
					Map<String, Object> thumbnails = new HashMap<String, Object>();
					thumbnails.put(URL, storageArea.getCdnDirectPath() + String.valueOf(object[3]) + String.valueOf(object[4]));
					item.put(THUMBNAILS, thumbnails);
				}
				if (object[5] != null) {
					Map<String, Object> resourceFormat = new HashMap<String, Object>();
					resourceFormat.put(VALUE, object[5]);
					resourceFormat.put(DISPLAY_NAME, object[6]);
					item.put(RESOURCEFORMAT, resourceFormat);
				}
				item.put(COLLECTION_ITEMS, getFolderItem(String.valueOf(object[1]), sharing, String.valueOf((object[2])), collectionType, itemLimit, fetchChildItem));
				item.put(ITEM_COUNT, this.getCollectionRepository().getCollectionItemCount(String.valueOf(object[1]), sharing, collectionType));
				item.put(SHARING, object[7]);
				item.put(COLLECTION_ITEM_ID, object[8]);
				if (object[9] != null) {
					item.put(GOALS, object[9]);
				}
				if (object[10] != null) {
					Map<String, Object> resourceSource = new HashMap<String, Object>();
					resourceSource.put(ATTRIBUTION, object[10]);
					resourceSource.put(DOMAIN_NAME, object[11]);
					item.put(RESOURCESOURCE, resourceSource);
				}
				items.add(item);
			}
		}
		return items;
	}

	@Override
	public List<Map<String, Object>> getFolderList(Integer limit, Integer offset, String gooruOid, String title, String username, boolean skipPagination) {
		List<Object[]> result = this.getCollectionRepository().getFolderList(limit, offset, gooruOid, title, username, skipPagination);
		List<Map<String, Object>> folderList = new ArrayList<Map<String, Object>>();
		if (result != null && result.size() > 0) {
			for (Object[] object : result) {
				Map<String, Object> folder = new HashMap<String, Object>();
				folder.put(GOORU_OID, object[0]);
				folder.put(TITLE, object[1]);
				folder.put(USER_NAME, object[2]);
				folder.put(CREATED_ON, object[3]);
				folder.put(LAST_MODIFIED, object[4]);
				folderList.add(folder);
			}
		}
		return folderList;
	}

	@Override
	public SearchResults<Code> getCollectionStandards(Integer codeId, String query, Integer limit, Integer offset, Boolean skipPagination, User user) {

		SearchResults<Code> result = new SearchResults<Code>();
		List<Object[]> list = this.getTaxonomyRespository().getCollectionStandards(codeId, query, limit, offset, skipPagination);
		List<Code> codeList = new ArrayList<Code>();
		for (Object[] object : list) {
			Code code = new Code();
			code.setCode((String) object[0]);
			code.setCodeId((Integer) object[1]);
			code.setLabel(((String) object[2]));
			code.setCodeUid((String) object[3]);
			codeList.add(code);
		}
		result.setSearchResults(codeList);
		return result;
	}

	@Override
	public Boolean resourceCopiedFrom(String gooruOid, String gooruUid) {
		Resource resource = collectionRepository.findResourceCopiedFrom(gooruOid, gooruUid);
		return resource != null ? true : false;
	}

	public StorageRepository getStorageRepository() {
		return storageRepository;
	}

	public TaxonomyRespository getTaxonomyRespository() {
		return taxonomyRespository;
	}
}
