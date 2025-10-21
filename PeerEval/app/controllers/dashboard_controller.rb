class DashboardController < ApplicationController
  before_action :authenticate_user!
  def home
    @presentations = Presentation.all # Adjust this query as needed for your requirements
  end
end
