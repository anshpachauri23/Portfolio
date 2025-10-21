class PresentationsController < ApplicationController
  before_action :authenticate_user!  # Ensure only logged-in users can access presentations

  def index
    @presentations = Presentation.all
  end

end
