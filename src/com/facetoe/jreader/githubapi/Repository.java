
package com.facetoe.jreader.githubapi;

import com.google.gson.annotations.SerializedName;

public class Repository {
    private String archive_url;
    private String assignees_url;
    private String blobs_url;
    private String branches_url;
    private String collaborators_url;
    private String comments_url;
    private String commits_url;
    private String compare_url;
    private String contents_url;
    private String contributors_url;
    private String description;
    private String downloads_url;
    private String events_url;
    private boolean fork;
    private String forks_url;
    private String full_name;
    private String git_commits_url;
    private String git_refs_url;
    private String git_tags_url;
    private String hooks_url;
    private String html_url;
    private Number id;
    private String issue_comment_url;
    private String issue_events_url;
    private String issues_url;
    private String keys_url;
    private String labels_url;
    private String languages_url;
    private String merges_url;
    private String milestones_url;
    private String name;
    private String notifications_url;
    private Owner owner;
    @SerializedName("private")
    private boolean isPrivate;
    private String pulls_url;
    private String releases_url;
    private String stargazers_url;
    private String statuses_url;
    private String subscribers_url;
    private String subscription_url;
    private String tags_url;
    private String teams_url;
    private String trees_url;
    private String url;

    public String getArchive_url() {
        return this.archive_url;
    }

    public void setArchive_url(String archive_url) {
        this.archive_url = archive_url;
    }

    public String getAssignees_url() {
        return this.assignees_url;
    }

    public void setAssignees_url(String assignees_url) {
        this.assignees_url = assignees_url;
    }

    public String getBlobs_url() {
        return this.blobs_url;
    }

    public void setBlobs_url(String blobs_url) {
        this.blobs_url = blobs_url;
    }

    public String getBranches_url() {
        return this.branches_url;
    }

    public void setBranches_url(String branches_url) {
        this.branches_url = branches_url;
    }

    public String getCollaborators_url() {
        return this.collaborators_url;
    }

    public void setCollaborators_url(String collaborators_url) {
        this.collaborators_url = collaborators_url;
    }

    public String getComments_url() {
        return this.comments_url;
    }

    public void setComments_url(String comments_url) {
        this.comments_url = comments_url;
    }

    public String getCommits_url() {
        return this.commits_url;
    }

    public void setCommits_url(String commits_url) {
        this.commits_url = commits_url;
    }

    public String getCompare_url() {
        return this.compare_url;
    }

    public void setCompare_url(String compare_url) {
        this.compare_url = compare_url;
    }

    public String getContents_url() {
        return this.contents_url;
    }

    public void setContents_url(String contents_url) {
        this.contents_url = contents_url;
    }

    public String getContributors_url() {
        return this.contributors_url;
    }

    public void setContributors_url(String contributors_url) {
        this.contributors_url = contributors_url;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDownloads_url() {
        return this.downloads_url;
    }

    public void setDownloads_url(String downloads_url) {
        this.downloads_url = downloads_url;
    }

    public String getEvents_url() {
        return this.events_url;
    }

    public void setEvents_url(String events_url) {
        this.events_url = events_url;
    }

    public boolean getFork() {
        return this.fork;
    }

    public void setFork(boolean fork) {
        this.fork = fork;
    }

    public String getForks_url() {
        return this.forks_url;
    }

    public void setForks_url(String forks_url) {
        this.forks_url = forks_url;
    }

    public String getFull_name() {
        return this.full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getGit_commits_url() {
        return this.git_commits_url;
    }

    public void setGit_commits_url(String git_commits_url) {
        this.git_commits_url = git_commits_url;
    }

    public String getGit_refs_url() {
        return this.git_refs_url;
    }

    public void setGit_refs_url(String git_refs_url) {
        this.git_refs_url = git_refs_url;
    }

    public String getGit_tags_url() {
        return this.git_tags_url;
    }

    public void setGit_tags_url(String git_tags_url) {
        this.git_tags_url = git_tags_url;
    }

    public String getHooks_url() {
        return this.hooks_url;
    }

    public void setHooks_url(String hooks_url) {
        this.hooks_url = hooks_url;
    }

    public String getHtml_url() {
        return this.html_url;
    }

    public void setHtml_url(String html_url) {
        this.html_url = html_url;
    }

    public Number getId() {
        return this.id;
    }

    public void setId(Number id) {
        this.id = id;
    }

    public String getIssue_comment_url() {
        return this.issue_comment_url;
    }

    public void setIssue_comment_url(String issue_comment_url) {
        this.issue_comment_url = issue_comment_url;
    }

    public String getIssue_events_url() {
        return this.issue_events_url;
    }

    public void setIssue_events_url(String issue_events_url) {
        this.issue_events_url = issue_events_url;
    }

    public String getIssues_url() {
        return this.issues_url;
    }

    public void setIssues_url(String issues_url) {
        this.issues_url = issues_url;
    }

    public String getKeys_url() {
        return this.keys_url;
    }

    public void setKeys_url(String keys_url) {
        this.keys_url = keys_url;
    }

    public String getLabels_url() {
        return this.labels_url;
    }

    public void setLabels_url(String labels_url) {
        this.labels_url = labels_url;
    }

    public String getLanguages_url() {
        return this.languages_url;
    }

    public void setLanguages_url(String languages_url) {
        this.languages_url = languages_url;
    }

    public String getMerges_url() {
        return this.merges_url;
    }

    public void setMerges_url(String merges_url) {
        this.merges_url = merges_url;
    }

    public String getMilestones_url() {
        return this.milestones_url;
    }

    public void setMilestones_url(String milestones_url) {
        this.milestones_url = milestones_url;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNotifications_url() {
        return this.notifications_url;
    }

    public void setNotifications_url(String notifications_url) {
        this.notifications_url = notifications_url;
    }

    public Owner getOwner() {
        return this.owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public boolean getPrivate() {
        return this.isPrivate;
    }

    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public String getPulls_url() {
        return this.pulls_url;
    }

    public void setPulls_url(String pulls_url) {
        this.pulls_url = pulls_url;
    }

    public String getReleases_url() {
        return this.releases_url;
    }

    public void setReleases_url(String releases_url) {
        this.releases_url = releases_url;
    }

    public String getStargazers_url() {
        return this.stargazers_url;
    }

    public void setStargazers_url(String stargazers_url) {
        this.stargazers_url = stargazers_url;
    }

    public String getStatuses_url() {
        return this.statuses_url;
    }

    public void setStatuses_url(String statuses_url) {
        this.statuses_url = statuses_url;
    }

    public String getSubscribers_url() {
        return this.subscribers_url;
    }

    public void setSubscribers_url(String subscribers_url) {
        this.subscribers_url = subscribers_url;
    }

    public String getSubscription_url() {
        return this.subscription_url;
    }

    public void setSubscription_url(String subscription_url) {
        this.subscription_url = subscription_url;
    }

    public String getTags_url() {
        return this.tags_url;
    }

    public void setTags_url(String tags_url) {
        this.tags_url = tags_url;
    }

    public String getTeams_url() {
        return this.teams_url;
    }

    public void setTeams_url(String teams_url) {
        this.teams_url = teams_url;
    }

    public String getTrees_url() {
        return this.trees_url;
    }

    public void setTrees_url(String trees_url) {
        this.trees_url = trees_url;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
