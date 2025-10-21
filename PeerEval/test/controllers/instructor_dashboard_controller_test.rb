require "test_helper"

class InstructorDashboardControllerTest < ActionDispatch::IntegrationTest
  test "should get home" do
    get instructor_dashboard_home_url
    assert_response :success
  end

  test "should get grade_submission" do
    get instructor_dashboard_grade_submission_url
    assert_response :success
  end
end
