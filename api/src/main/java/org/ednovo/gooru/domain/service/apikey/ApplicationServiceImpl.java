/////////////////////////////////////////////////////////////
// ApplicationServiceImpl.java
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
package org.ednovo.gooru.domain.service.apikey;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.ednovo.gooru.core.api.model.ActionResponseDTO;
import org.ednovo.gooru.core.api.model.ApiKey;
import org.ednovo.gooru.core.api.model.CustomTableValue;
import org.ednovo.gooru.core.api.model.Organization;
import org.ednovo.gooru.core.api.model.PartyCustomField;
import org.ednovo.gooru.core.api.model.User;
import org.ednovo.gooru.core.application.util.CustomProperties;
import org.ednovo.gooru.core.constant.ConstantProperties;
import org.ednovo.gooru.core.constant.ParameterProperties;
import org.ednovo.gooru.domain.service.BaseServiceImpl;
import org.ednovo.gooru.domain.service.PartyService;
import org.ednovo.gooru.domain.service.party.OrganizationService;
import org.ednovo.gooru.infrastructure.persistence.hibernate.apikey.ApplicationRepository;
import org.ednovo.gooru.infrastructure.persistence.hibernate.customTable.CustomTableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

@Service
public class ApplicationServiceImpl extends BaseServiceImpl implements ApplicationService,ParameterProperties {

	@Autowired
	private ApplicationRepository apiKeyRepository;

	@Autowired
	private PartyService partyService;
	
	@Autowired
	private OrganizationService organizationService;
	
	@Autowired
	private CustomTableRepository customTableRepository;

	@Override
	public List<ApiKey> findApplicationByOrganization(String organizationUid){
		return apiKeyRepository.getApplicationByOrganization(organizationUid);
	}

	@Override
	public ActionResponseDTO<ApiKey> saveApplication(ApiKey apikey, User user) throws Exception{
		Errors error = validateApiKey(apikey);
		if (!error.hasErrors()) {
			PartyCustomField partyCustomField = partyService.getPartyCustomeField(user.getPartyUid(), ConstantProperties.ORG_ADMIN_KEY, user);
			if(partyCustomField != null && partyCustomField.getOptionalValue() != null){
				Organization organization = organizationService.getOrganizationById(partyCustomField.getOptionalValue());
				if(organization == null){
					throw new RuntimeException("Organization not found !");
				}
				apikey.setActiveFlag(1);
				apikey.setSecretKey(UUID.randomUUID().toString());
				apikey.setKey(UUID.randomUUID().toString());
				apikey.setOrganization(organization);
				apikey.setLimit(-1);
				apikey.setDescription(apikey.getDescription());
				CustomTableValue type = this.getCustomTableRepository().getCustomTableValue(CustomProperties.Table.APPLICATION_STATUS.getTable(), CustomProperties.ApplicationStatus.DEVELOPMENT.getApplicationStatus());
				apikey.setStatus(type.getValue());
				apikey.setComment(apikey.getComment());
				apiKeyRepository.save(apikey);
			}
			else {
				throw new RuntimeException("Admin organization not found in custom fields");
			}
		}
		return new ActionResponseDTO<ApiKey>(apikey, error);
	}
	
	private Errors validateApiKey(ApiKey apiKey) {
		final Errors errors = new BindException(apiKey, API_KEY);
		rejectIfNull(errors, apiKey, APP_NAME, GL0056, generateErrorMessage(GL0056, APP_NAME));
		rejectIfNull(errors, apiKey, APP_URL, GL0056, generateErrorMessage(GL0056, APP_URL));
		return errors;
	}

	@Override
	public ActionResponseDTO<ApiKey> updateApplication(ApiKey apikey, User user)
			throws Exception {
		Errors error = validateApiKey(apikey);
		ApiKey existingApiKey = apiKeyRepository.getApplicationByAppKey(apikey.getKey());
		if (!error.hasErrors()) {
			if(apikey.getDescription() != null){
			 existingApiKey.setDescription(apikey.getDescription());
			}
			if(apikey.getAppName() != null){
			 existingApiKey.setAppName(apikey.getAppName());
			}
			if(apikey.getAppURL() != null){
			 existingApiKey.setAppURL(apikey.getAppURL());
			}
			existingApiKey.setLastUpdatedUserUid(user.getPartyUid());
			existingApiKey.setLastUpdatedDate(new Date(System.currentTimeMillis()));
			
			if(apikey.getStatus() != null){
			 existingApiKey.setStatus(apikey.getStatus());
			}
			if(apikey.getComment() != null){
			 existingApiKey.setComment(apikey.getComment());
			}
			apiKeyRepository.save(existingApiKey);
		}
		return new ActionResponseDTO<ApiKey>(existingApiKey, error);
	}
	public CustomTableRepository getCustomTableRepository() {
		return customTableRepository;
	}

}