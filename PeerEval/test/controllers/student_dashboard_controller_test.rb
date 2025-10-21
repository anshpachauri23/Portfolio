require "test_helper"

class StudentDashboardControllerTest < ActionDispatch::IntegrationTest
  test "should get home" do
    get student_dashboard_home_url
    assert_response :success
  end

  test "should get submit_homework" do
    get student_dashboard_submit_homework_url
    assert_response :success
  end
end
