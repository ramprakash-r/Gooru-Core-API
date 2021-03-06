package org.ednovo.gooru.core.api.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ednovo.gooru.core.application.util.CollectionMetaInfo;

public class Collection extends Resource implements Versionable {
	/**
	 * 
	 */
private static final long serialVersionUID = -3271310636333972691L;

private static final String INDEX_TYPE = "scollection";

private String collectionType;

private String narrationLink;

private String notes;

private String keyPoints;

private String language;

private String goals;

private String estimatedTime;

private Set<CollectionItem> collectionItems;

private List<User> collaborators;

private ContentAssociation contentAssociation;

private Map<Integer, List<Code>> taxonomySetMapping;

private Set<CollectionTaskAssoc> collectionTaskItems;

private CollectionMetaInfo metaInfo;

private String network;

private CollectionItem collectionItem;

private CustomTableValue buildType;

private Boolean mailNotification;

private Map<String, Object> lastModifiedUser;

private String ideas;

private String questions;

private String performanceTasks;

private String languageObjective;

private String audience;

private String learningSkills;

private String instructionalMethod;

public String getIdeas() {
	return ideas;
}

public void setIdeas(String ideas) {
	this.ideas = ideas;
}

public String getQuestions() {
	return questions;
}

public void setQuestions(String questions) {
	this.questions = questions;
}

public String getPerformanceTasks() {
	return performanceTasks;
}

public void setPerformanceTasks(String performanceTasks) {
	this.performanceTasks = performanceTasks;
}

public String getLanguageObjective() {
	return languageObjective;
}

public void setLanguageObjective(String languageObjective) {
	this.languageObjective = languageObjective;
}

public String getAudience() {
	return audience;
}

public void setAudience(String audience) {
	this.audience = audience;
}

public String getLearningSkills() {
	return learningSkills;
}

public void setLearningSkills(String learningSkills) {
	this.learningSkills = learningSkills;
}

public String getInstructionalMethod() {
	return instructionalMethod;
}

public void setInstructionalMethod(String instructionalMethod) {
	this.instructionalMethod = instructionalMethod;
}

public Map<String, Object> getLastModifiedUser() {
	return lastModifiedUser;
}

public void setLastModifiedUser(Map<String, Object> lastModifiedUser) {
	this.lastModifiedUser = lastModifiedUser;
}

public Collection() {
	super();
}

public String getCollectionType() {
	return collectionType;
}

public void setCollectionType(String collectionType) {
	this.collectionType = collectionType;
}

public String getNarrationLink() {
	return narrationLink;
}

public void setNarrationLink(String narrationLink) {
	this.narrationLink = narrationLink;
}

public String getNotes() {
	return notes;
}

public void setNotes(String notes) {
	this.notes = notes;
}

public String getKeyPoints() {
	return keyPoints;
}

public void setKeyPoints(String keyPoints) {
	this.keyPoints = keyPoints;
}

public String getLanguage() {
	return language;
}

public void setLanguage(String language) {
	this.language = language;
}

public String getGoals() {
	return goals;
}

public void setGoals(String goals) {
	this.goals = goals;
}

public String getEstimatedTime() {
	return estimatedTime;
}

public void setEstimatedTime(String estimatedTime) {
	this.estimatedTime = estimatedTime;
}

public Set<CollectionItem> getCollectionItems() {
	return collectionItems;
}

public void setCollectionItems(Set<CollectionItem> collectionItems) {
	this.collectionItems = collectionItems;
}

public void setMetaInfo(CollectionMetaInfo metaInfo) {
	this.metaInfo = metaInfo;
}

public CollectionMetaInfo getMetaInfo() {
	return metaInfo;
}

public void setCollaborators(List<User> collaborators) {
	this.collaborators = collaborators;
}

public List<User> getCollaborators() {
	return collaborators;
}

public String getNetwork() {
	return network;
}

public void setNetwork(String network) {
	this.network = network;
}

public ContentAssociation getContentAssociation() {
	return contentAssociation;
}

public void setContentAssociation(ContentAssociation contentAssociation) {
	this.contentAssociation = contentAssociation;
}

public void setTaxonomySetMapping(
		Map<Integer, List<Code>> taxonomySetMapping) {
	this.taxonomySetMapping = taxonomySetMapping;
}

public Map<Integer, List<Code>> getTaxonomySetMapping() {
	return taxonomySetMapping;
}

@Override
public String getEntityId() {
	return getGooruOid();
}

public void setCollectionTaskItems(
		Set<CollectionTaskAssoc> collectionTaskItems) {
	this.collectionTaskItems = collectionTaskItems;
}

public Set<CollectionTaskAssoc> getCollectionTaskItems() {
	return collectionTaskItems;
}

@Override
public String getIndexType() {
	return INDEX_TYPE;
}

public void setCollectionItem(CollectionItem collectionItem) {
	this.collectionItem = collectionItem;
}

public CollectionItem getCollectionItem() {
	return collectionItem;
}

public void setBuildType(CustomTableValue buildType) {
	this.buildType = buildType;
}

public CustomTableValue getBuildType() {
	return buildType;
}

public Boolean getMailNotification() {
	return mailNotification;
}

public void setMailNotification(Boolean mailNotification) {
	if (mailNotification == null) {
		mailNotification = true;
	}
	this.mailNotification = mailNotification;
}
}
