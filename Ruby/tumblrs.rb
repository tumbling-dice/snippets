$:.unshift File.dirname(__FILE__)
require "async_task.rb"
require "tumblr_client"

class TumblrReader
	
	Domain = "xxx.tumblr.com"
	attr_accessor:limit
	
	class TypeInfo
		def initialize(type, offset, total_posts)
			@type = type
			@info_hash = {type => {:offset => offset, :total_posts => total_posts, :is_read => false}}
		end
		
		def type; @type ;end
		
		def type=(val)
			@type = val
			
			unless @info_hash.key?(val)
				@info_hash = {type => {:offset => 0, :total_posts => 0, :is_read => false}}
			end
		end
		
		def offset(); @info_hash[@type][:offset] ;end
		def offset=(val); @info_hash[@type][:offset] = val; end
		
		def total_posts(); @info_hash[@type][:total_posts]; end
		def total_posts=(val); @info_hash[@type][:total_posts] = val; end
		def is_read(); @info_hash[@type][:is_read]; end
		def is_read=(val); @info_hash[@type][:is_read] = val; end
		
		def is_end
			if is_read then
				total_posts == 0 || offset >= total_posts
			else
				return false
			end
		end
	end
	
	def initialize(type="text", limit=20)
		Tumblr.configure {|config|
			config.consumer_key = ""
			config.consumer_secret = ""
			config.oauth_token = ""
			config.oauth_token_secret = ""
		}
		
		@client = Tumblr::Client.new
		@type = type
		@limit = limit
		@type_info = TypeInfo.new(type, 0, 0)
	end
	
	def change_type(new_type); @type_info.type = new_type; end
	def is_end; @type_info.is_end; end
	
	def get_posts_async
		
		if is_end then return Array.new end
		
		# 初回時はtotal_postsを取得する必要がある
		unless @type_info.is_read then
			
			r = @client.posts(Domain, :type => @type_info.type, :limit => @limit, :offset => 0);
			@type_info.offset += @limit
			@type_info.total_posts = r["total_posts"]
			@type_info.is_read = true
			
			return AsyncTask.new(lambda{|x| x}, r["posts"]).continue{|t|
				t.result.each{|x| yield x } if block_given?
			}.start
		else
			return AsyncTask.new(lambda{
				
				if @type_info.is_end then return Array.new end
				
				offset = @type_info.offset
				@type_info.offset += @limit
				
				return @client.posts(Domain, :type => @type_info.type, :limit => @limit, :offset => offset)["posts"]
				
			}).continue{|t|
				t.result.each{|x| yield x } if block_given?
			}.start
		end
		
	end
	
end

tr = TumblrReader.new
tasks = Array.new

puts "Get Tumblr's posts..."

while(!tr.is_end) do
	tasks.push(tr.get_posts_async{|x|
		# TODO:Tumblrの情報でなんかする
	})
end

tasks.each{|t|
	t.wait
}
