require "thread"

class AsyncTask
  
  def initialize(on_background, *params)
    this = self
    
    @task = lambda {
      Thread.new {
        begin
          @result = on_background.call(*params)
        rescue Exception => ex
          @exception = ex
        ensure
          @is_end = true
          @on_exuecuted.call(this) if @on_exuecuted
          kill
        end
      }
    }
  end
  
  def start
    @thread = @task.call unless @thread
    return self
  end
  
  def wait
    @thread.join if @thread
  end
  
  def result
    raise exception if @exception
    return @result
  end
  
  def status
    @thread.status if @thread
  end
  
  def kill
    Thread.kill(@thread) if @thread
  end
  
  def continue(&on_exuecuted)
    
    if @is_end then
      on_exuecuted.call(self)
    end
    
    @on_exuecuted = on_exuecuted
    return self
  end
  
end