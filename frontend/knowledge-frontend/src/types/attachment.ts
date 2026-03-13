export interface AttachmentDTO {
  type: 'file' | 'video' | 'link'
  name: string
  url: string
  size?: number
}
