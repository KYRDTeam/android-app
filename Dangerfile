# github comment settings
github.dismiss_out_of_range_messages

declared_trivial = github.pr_title.include? "#trivial"
declared_feature = github.pr_title.include? "#feature"
declared_wip = github.pr_labels.include?("WIP")


# PR to master branch.
if github.branch_for_base == "master" && github.branch_for_head.include?("release")
  message("This is PR for submit to Google.")
  return
elsif github.branch_for_base == "master" && !github.branch_for_head.include?("release")
  fail("Please close PR. Only the release branch can PR to master branch.")
end

# Warn when WIP
warn("PR is classed as Work in Progress. Write comment `run danger` when done your work") if declared_wip

# Warn when there is not milestone.
has_milestone = github.pr_json["milestone"] != nil
warn("This PR does not refer to an existing milestone", sticky: false) unless has_milestone

# Warn when there is a big PR
warn("This PR is too big. You should divide this PR into smaller PRs.") if git.lines_of_code > 500

# Warn when there is not Github issue.
has_github_issue_in_pr_title = github.pr_title.match /(#\d+)/
has_github_issue_in_pr_body = github.pr_body.match /(#\d+)/

if !declared_trivial && !declared_feature
	if has_github_issue_in_pr_title
	    github_issue_number_title = has_github_issue_in_pr_title[1].match /(\d+)/
		message("Github Issue: <a href='https://github.com/KyberNetwork/kyberswap-android/issues/#{github_issue_number_title[1]}'>https://github.com/KyberNetwork/kyberswap-android/issues/#{github_issue_number_title[1]}</a>")
	elsif has_github_issue_in_pr_body
	    github_issue_number_body = has_github_issue_in_pr_body[1].match /(\d+)/
		message("Github Issue: <a href='https://github.com/KyberNetwork/kyberswap-android/issues/#{github_issue_number_body[1]}'>https://github.com/KyberNetwork/kyberswap-android/issues/#{github_issue_number_body[1]}</a>")
	else
		warn("The Github's issue number not found. Please write Github's issue number on this PR title or body. ")
	end
end

# Warn when not update QA check list
has_qa_check_lists = github.pr_body.match /(<Write qa check lists, remove this line>)/
warn("Please Write qa check lists.") if has_qa_check_lists && !declared_trivial

# Reviewers
has_assignee = github.pr_json["assignee"] != nil
warn("No Assign", sticky: false) unless has_assignee

# Findbugs
#findbugs.gradle_module = "app"
#findbugs.gradle_task = "app:findbugs"
#findbugs.report_file = "app/build/reports/findbugs/findbugs.xml"
#findbugs.report
checkstyle_format.base_path = Dir.pwd
checkstyle_format.report 'app/build/reports/detekt/detekt-checkstyle.xml'

# ktlint
github.dismiss_out_of_range_messages
checkstyle_format.base_path = Dir.pwd
checkstyle_format.report 'app/build/reports/ktlint/ktlint-checkstyle-report.xml'

# detekt
checkstyle_format.base_path = Dir.pwd
checkstyle_format.report 'app/build/reports/detekt/detekt-checkstyle.xml'

# AndroidLint
android_lint.report_file = "app/build/reports/lint-results.xml"
android_lint.skip_gradle_task = true
android_lint.severity = "Error"
android_lint.lint(inline_mode: true)

# Menthon when passed all checks
return unless status_report[:errors].length.zero? && status_report[:warnings].length.zero?
message("LGTM :+1:\nWaiting for your review!\n@toidv")

